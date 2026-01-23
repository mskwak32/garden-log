package com.mskwak.plant.model

import androidx.compose.runtime.Immutable
import com.mskwak.domain.model.Diary
import java.time.LocalDate

@Immutable
data class DiaryListItemUiModel(
    val id: Int,
    val date: LocalDate,
    val content: String,
    val imagePath: String?,
    val plantName: String? = null
)

fun Diary.toDiaryListItemUiModel(): DiaryListItemUiModel {
    return DiaryListItemUiModel(
        id = id,
        date = createdDate,
        content = memo,
        imagePath = pictureList?.firstOrNull()?.path
    )
}