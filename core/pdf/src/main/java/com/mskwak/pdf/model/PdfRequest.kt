package com.mskwak.pdf.model

import java.time.LocalDate

data class PdfRequest(
    val plantName: String,
    val includeFirstPage: Boolean,
    val plantDetail: PlantDetailPdf?,
    val diaries: List<DiaryPdf>
)

data class PlantDetailPdf(
    val name: String,
    val imagePath: String?,
    val plantingDate: LocalDate?,
    val wateringCycle: Int,
    val memo: String?,
    val harvestDate: LocalDate? = null,
    val harvestMemo: String? = null
)

data class DiaryPdf(
    val date: LocalDate,
    val content: String,
    val imagePaths: List<String>,
    val hasWateringRecord: Boolean = false
)
