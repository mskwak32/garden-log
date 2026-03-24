package com.mskwak.domain.model

import java.time.LocalDate

data class ExportRequest(
    val plantName: String,
    val includeFirstPage: Boolean,
    val plantDetail: PlantDetailExport?,
    val diaries: List<DiaryExport>
)

data class PlantDetailExport(
    val name: String,
    val imagePath: String?,
    val plantingDate: LocalDate?,
    val wateringCycle: Int,
    val memo: String?,
    val harvestDate: LocalDate? = null,
    val harvestMemo: String? = null
)

data class DiaryExport(
    val date: LocalDate,
    val content: String,
    val imagePaths: List<String>,
    val hasWateringRecord: Boolean = false
)
