package com.mskwak.plant.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class DiaryListItemUiModel(
    val id: Int,
    val date: LocalDate,
    val content: String,
    val imagePath: String?,
    val plantName: String? = null,
    val createdDate: LocalDate? = null
)