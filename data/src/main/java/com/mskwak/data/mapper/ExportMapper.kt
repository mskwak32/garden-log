package com.mskwak.data.mapper

import com.mskwak.domain.model.DiaryExport
import com.mskwak.domain.model.ExportRequest
import com.mskwak.domain.model.ExportedFileInfo
import com.mskwak.domain.model.PlantDetailExport
import com.mskwak.pdf.model.DiaryPdf
import com.mskwak.pdf.model.PdfFileInfo
import com.mskwak.pdf.model.PdfRequest
import com.mskwak.pdf.model.PlantDetailPdf

fun ExportRequest.toPdfRequest(): PdfRequest = PdfRequest(
    plantName = plantName,
    includeFirstPage = includeFirstPage,
    plantDetail = plantDetail?.toPlantDetailPdf(),
    diaries = diaries.map { it.toDiaryPdf() }
)

fun PlantDetailExport.toPlantDetailPdf(): PlantDetailPdf = PlantDetailPdf(
    name = name,
    imagePath = imagePath,
    plantingDate = plantingDate,
    wateringCycle = wateringCycle,
    memo = memo,
    harvestDate = harvestDate,
    harvestMemo = harvestMemo
)

fun DiaryExport.toDiaryPdf(): DiaryPdf = DiaryPdf(
    date = date,
    content = content,
    imagePaths = imagePaths,
    hasWateringRecord = hasWateringRecord
)

fun PdfFileInfo.toExportedFileInfo(): ExportedFileInfo = ExportedFileInfo(
    contentUri = contentUri.toString(),
    fileName = fileName,
    createdAt = createdAt,
    fileSize = fileSize
)
