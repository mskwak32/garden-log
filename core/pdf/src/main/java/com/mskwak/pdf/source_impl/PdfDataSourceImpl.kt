package com.mskwak.pdf.source_impl

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.mskwak.pdf.PdfDataSource
import com.mskwak.pdf.R
import com.mskwak.pdf.model.DiaryPdf
import com.mskwak.pdf.model.PdfFileInfo
import com.mskwak.pdf.model.PdfRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.format.DateTimeFormatter
import javax.inject.Inject

internal class PdfDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PdfDataSource {

    companion object {
        private const val FOLDER_NAME = "GardenLog"
        private const val RELATIVE_PATH = "Download/$FOLDER_NAME/"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
        private val DIARY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")

        // A4 @ 72 dpi (points)
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842

        // 여백 (px)
        private const val MARGIN = 56f

        // 본문 영역 너비
        private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

        // 이미지 2장 배치: 각 이미지 너비 (가로:세로 = 4:3)
        private const val IMAGE_GAP = 8f
        private const val IMAGE_WIDTH = (CONTENT_WIDTH - IMAGE_GAP) / 2f
        private const val IMAGE_HEIGHT = IMAGE_WIDTH * 3f / 4f

        // 식물 대표 이미지 (첫 페이지): 전체 너비, 동일 비율
        private const val PLANT_IMAGE_WIDTH = CONTENT_WIDTH
        private const val PLANT_IMAGE_HEIGHT = PLANT_IMAGE_WIDTH / 1.5f

        // 페이지 하단 임계값: 남은 높이가 이 값 이하이면 다음 페이지로
        private const val PAGE_BOTTOM_THRESHOLD = 120f

        // 텍스트 크기
        private const val TITLE_TEXT_SIZE = 22f
        private const val LABEL_TEXT_SIZE = 13f
        private const val DATE_TEXT_SIZE = 14f
        private const val CONTENT_TEXT_SIZE = 12f

        // 줄 간격 추가값
        private const val LINE_SPACING_EXTRA = 4f

        // 이미지 품질 설정을 위한 DPI 스케일 (120 DPI 목표: 120 / 72 ≒ 1.666)
        private const val DPI_SCALE = 1.67f
    }

    override suspend fun generatePdf(request: PdfRequest): Uri = withContext(Dispatchers.IO) {
        val startDate = request.diaries.minOf { it.date }
        val endDate = request.diaries.maxOf { it.date }
        val fileName = "${request.plantName}_${startDate.format(DATE_FORMATTER)}_${
            endDate.format(DATE_FORMATTER)
        }.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, RELATIVE_PATH)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: error("MediaStore insert 실패")

        try {
            resolver.openOutputStream(uri)!!.use { outputStream ->
                val pdfDocument = PdfDocument()

                val writer = PageWriter(pdfDocument)

                if (request.includeFirstPage && request.plantDetail != null) {
                    writer.addPlantDetailPage(request)
                }

                writer.addDiaryPages(request.diaries)

                pdfDocument.writeTo(outputStream)
                pdfDocument.close()
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            Timber.e(e, "PDF 생성 실패")
            throw e
        }

        Timber.d("PDF 생성 완료: $uri")
        uri
    }

    override suspend fun getExportedFiles(): List<PdfFileInfo> = withContext(Dispatchers.IO) {
        val files = mutableListOf<PdfFileInfo>()

        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DATE_ADDED,
            MediaStore.Downloads.SIZE
        )
        val selection = "${MediaStore.Downloads.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(RELATIVE_PATH)
        val sortOrder = "${MediaStore.Downloads.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri =
                    Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
                files.add(
                    PdfFileInfo(
                        contentUri = uri,
                        fileName = cursor.getString(nameCol),
                        createdAt = cursor.getLong(dateCol) * 1000L, // sec → ms
                        fileSize = cursor.getLong(sizeCol)
                    )
                )
            }
        }

        files
    }

    override suspend fun deleteFile(contentUri: Uri) = withContext(Dispatchers.IO) {
        val deleted = context.contentResolver.delete(contentUri, null, null)
        Timber.d("PDF 삭제: $contentUri, deleted=$deleted")
    }

    /**
     * PDF 페이지를 순차적으로 추가하는 내부 헬퍼.
     * 현재 페이지와 y 위치를 추적하며, 필요 시 새 페이지를 생성한다.
     */
    private inner class PageWriter(private val pdfDocument: PdfDocument) {
        private var pageIndex = 1
        private var page: PdfDocument.Page = newPage()
        private var canvas: Canvas = page.canvas
        private var y: Float = MARGIN

        private fun newPage(): PdfDocument.Page {
            val pageInfo =
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex++).create()
            return pdfDocument.startPage(pageInfo)
        }

        private fun finishCurrentAndStartNew() {
            pdfDocument.finishPage(page)
            page = newPage()
            canvas = page.canvas
            y = MARGIN
        }

        fun finish() {
            pdfDocument.finishPage(page)
        }

        /** 남은 페이지 높이 */
        private fun remainingHeight() = PAGE_HEIGHT - MARGIN - y

        /** 필요 높이가 남은 공간보다 크면 새 페이지 */
        private fun ensureSpace(needed: Float) {
            if (remainingHeight() < needed) {
                finishCurrentAndStartNew()
            }
        }

        fun addPlantDetailPage(request: PdfRequest) {
            val detail = request.plantDetail ?: return

            val titlePaint = TextPaint().apply {
                color = Color.BLACK
                textSize = TITLE_TEXT_SIZE
                isFakeBoldText = true
                isAntiAlias = true
            }
            val labelPaint = TextPaint().apply {
                color = Color.DKGRAY
                textSize = LABEL_TEXT_SIZE
                isAntiAlias = true
            }

            // 식물 대표 이미지
            if (!detail.imagePath.isNullOrBlank()) {
                drawPlantImage(detail.imagePath)
                y += 16f
            }

            // 제목
            drawTextLine(detail.name, titlePaint)
            y += 24f

            fun addRow(label: String, value: String?) {
                if (value.isNullOrBlank()) return
                drawMultilineText("$label: $value", labelPaint)
                y += 8f
            }

            addRow(
                context.getString(R.string.pdf_label_planting_date),
                detail.plantingDate?.format(DIARY_DATE_FORMATTER)
            )
            if (detail.wateringCycle > 0) {
                addRow(
                    context.getString(R.string.pdf_label_watering_cycle),
                    context.getString(R.string.pdf_watering_cycle_format, detail.wateringCycle)
                )
            }
            addRow(context.getString(R.string.pdf_label_memo), detail.memo)
            addRow(
                context.getString(R.string.pdf_label_harvest_date),
                detail.harvestDate?.format(DIARY_DATE_FORMATTER)
            )
            addRow(context.getString(R.string.pdf_label_harvest_memo), detail.harvestMemo)

            // 첫 페이지 끝내고 새 페이지 시작
            pdfDocument.finishPage(page)
            page = newPage()
            canvas = page.canvas
            y = MARGIN
        }

        fun addDiaryPages(diaries: List<DiaryPdf>) {
            val datePaint = TextPaint().apply {
                color = Color.BLACK
                textSize = DATE_TEXT_SIZE
                isFakeBoldText = true
                isAntiAlias = true
            }
            val contentPaint = TextPaint().apply {
                color = Color.DKGRAY
                textSize = CONTENT_TEXT_SIZE
                isAntiAlias = true
            }
            val waterDropDrawable = context.getDrawable(R.drawable.ic_water_drop_blue)

            diaries.forEach { diary ->
                // 일기 시작 지점이 페이지 하단 임계값 이하이면 다음 페이지로
                if (remainingHeight() < PAGE_BOTTOM_THRESHOLD) {
                    finishCurrentAndStartNew()
                }

                y += 30f // spacingBefore

                // 날짜 헤더 (+ 물주기 기록 있으면 물방울 아이콘)
                val dateText = diary.date.format(DIARY_DATE_FORMATTER)
                ensureSpace(datePaint.textSize + LINE_SPACING_EXTRA)
                canvas.drawText(dateText, MARGIN, y + datePaint.textSize, datePaint)
                if (diary.hasWateringRecord && waterDropDrawable != null) {
                    val textWidth = datePaint.measureText(dateText)
                    val iconSize = datePaint.textSize.toInt()
                    val iconLeft = (MARGIN + textWidth + 8f).toInt()
                    val iconTop = y.toInt()
                    waterDropDrawable.setBounds(
                        iconLeft,
                        iconTop,
                        iconLeft + iconSize,
                        iconTop + iconSize
                    )
                    waterDropDrawable.draw(canvas)
                }
                y += datePaint.textSize + LINE_SPACING_EXTRA
                y += 6f // spacingAfter

                // 이미지 (2장씩 한 행)
                if (diary.imagePaths.isNotEmpty()) {
                    drawImageRows(diary.imagePaths)
                }

                // 일기 내용
                if (diary.content.isNotBlank()) {
                    drawMultilineText(diary.content, contentPaint)
                    y += 8f
                }
            }

            finish()
        }

        /**
         * 단일 텍스트 라인을 그린다 (줄바꿈 없음).
         */
        private fun drawTextLine(text: String, paint: TextPaint) {
            ensureSpace(paint.textSize + LINE_SPACING_EXTRA)
            canvas.drawText(text, MARGIN, y + paint.textSize, paint)
            y += paint.textSize + LINE_SPACING_EXTRA
        }

        /**
         * 다중 라인 텍스트를 StaticLayout으로 그린다.
         * 페이지 경계를 넘으면 새 페이지에서 이어서 그린다.
         */
        private fun drawMultilineText(text: String, paint: TextPaint) {
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, CONTENT_WIDTH.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(LINE_SPACING_EXTRA, 1f)
                .setIncludePad(false)
                .build()

            val lineHeight = paint.textSize + LINE_SPACING_EXTRA

            for (i in 0 until layout.lineCount) {
                ensureSpace(lineHeight)
                val lineStart = layout.getLineStart(i)
                val lineEnd = layout.getLineVisibleEnd(i)
                val lineText = text.substring(lineStart, lineEnd)
                canvas.drawText(lineText, MARGIN, y + paint.textSize, paint)
                y += lineHeight
            }
        }

        /**
         * 식물 대표 이미지를 전체 너비로 그린다.
         */
        private fun drawPlantImage(imagePath: String) {
            ensureSpace(PLANT_IMAGE_HEIGHT)
            try {
                val targetWidth = (PLANT_IMAGE_WIDTH * DPI_SCALE).toInt()
                val targetHeight = (PLANT_IMAGE_HEIGHT * DPI_SCALE).toInt()

                val bitmap = decodeBitmap(imagePath, targetWidth, targetHeight) ?: return
                val cropped = cropToRatio(bitmap, 1.5f)

                @Suppress("UseKtx")
                val scaled = Bitmap.createScaledBitmap(
                    cropped,
                    targetWidth,
                    targetHeight,
                    true
                )
                val destRect = RectF(MARGIN, y, MARGIN + PLANT_IMAGE_WIDTH, y + PLANT_IMAGE_HEIGHT)
                canvas.drawBitmap(
                    scaled,
                    null,
                    destRect,
                    Paint().apply { isAntiAlias = true; isFilterBitmap = true })
                if (scaled !== cropped) scaled.recycle()
                if (cropped !== bitmap) cropped.recycle()
                bitmap.recycle()
                y += PLANT_IMAGE_HEIGHT
            } catch (e: Exception) {
                Timber.e(e, "식물 이미지 로드 실패: $imagePath")
            }
        }

        /**
         * 이미지를 1.5:1 비율로 crop하여 가로 2장씩 배치한다.
         */
        private fun drawImageRows(imagePaths: List<String>) {
            val rowHeight = IMAGE_HEIGHT + IMAGE_GAP

            imagePaths.chunked(2).forEach { pair ->
                ensureSpace(rowHeight)

                var x = MARGIN
                pair.forEach { path ->
                    drawImage(path, x, y)
                    x += IMAGE_WIDTH + IMAGE_GAP
                }

                y += rowHeight
            }
        }

        private fun drawImage(imagePath: String, x: Float, y: Float) {
            try {
                val targetWidth = (IMAGE_WIDTH * DPI_SCALE).toInt()
                val targetHeight = (IMAGE_HEIGHT * DPI_SCALE).toInt()

                val bitmap = decodeBitmap(imagePath, targetWidth, targetHeight) ?: return
                val cropped = cropToRatio(bitmap, 4f / 3f)

                @Suppress("UseKtx")
                val scaled = Bitmap.createScaledBitmap(
                    cropped,
                    targetWidth,
                    targetHeight,
                    true
                )
                val destRect = RectF(x, y, x + IMAGE_WIDTH, y + IMAGE_HEIGHT)
                canvas.drawBitmap(
                    scaled,
                    null,
                    destRect,
                    Paint().apply { isAntiAlias = true; isFilterBitmap = true })
                if (scaled !== cropped) scaled.recycle()
                if (cropped !== bitmap) cropped.recycle()
                bitmap.recycle()
            } catch (e: Exception) {
                Timber.e(e, "이미지 로드 실패: $imagePath")
            }
        }

        /**
         * 지정된 경로의 이미지를 목표 크기(reqWidth, reqHeight)에 맞춰 효율적으로 로드한다.
         * BitmapFactory.Options를 사용하여 실제 비트맵을 메모리에 올리기 전에 크기를 먼저 확인하고,
         * 적절한 inSampleSize를 계산하여 메모리 사용량을 최적화한다.
         */
        private fun decodeBitmap(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
            val options = BitmapFactory.Options().apply {
                // 실제 비트맵 데이터는 로드하지 않고 이미지의 정보(크기, 타입 등)만 읽어온다.
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            // 목표 크기에 가장 근접한(2의 거듭제곱 단위) 샘플링 비율을 계산한다.
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(path, options)
        }

        /**
         * 원본 이미지 크기와 목표 크기를 비교하여 가장 적절한 inSampleSize 값을 계산한다.
         * inSampleSize는 2의 거듭제곱(1, 2, 4, 8...) 값을 가지며, 값이 클수록 로드되는 이미지 해상도는 낮아지고
         * 메모리는 절약된다.
         */
        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val (height: Int, width: Int) = options.outHeight to options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // 목표 크기보다 작아지기 직전까지 inSampleSize를 2배씩 키운다.
                while (halfHeight / inSampleSize >= reqHeight
                    && halfWidth / inSampleSize >= reqWidth
                ) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }

    /**
     * 비트맵을 지정한 가로:세로 비율로 center crop한다.
     */
    private fun cropToRatio(bitmap: Bitmap, ratio: Float): Bitmap {
        val srcWidth = bitmap.width
        val srcHeight = bitmap.height
        val srcRatio = srcWidth.toFloat() / srcHeight.toFloat()

        val (cropWidth, cropHeight) = if (srcRatio > ratio) {
            // 더 넓은 경우 → 가로 크롭
            val w = (srcHeight * ratio).toInt()
            w to srcHeight
        } else {
            // 더 높은 경우 → 세로 크롭
            val h = (srcWidth / ratio).toInt()
            srcWidth to h
        }

        val x = (srcWidth - cropWidth) / 2
        val y = (srcHeight - cropHeight) / 2

        return Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight)
    }
}
