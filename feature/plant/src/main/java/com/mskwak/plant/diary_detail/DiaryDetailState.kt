package com.mskwak.plant.diary_detail

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import java.time.LocalDate

@Immutable
data class DiaryDetailState(
    val plantName: String = "",
    val diaryDate: LocalDate = LocalDate.now(),
    val picturePaths: List<String> = emptyList(),
    val memo: String = "",
    val isHarvested: Boolean = false,
    val isWatered: Boolean = false
) : ViewState

sealed interface DiaryDetailEvent : ViewEvent {
    data object BackClick : DiaryDetailEvent
    data object EditClick : DiaryDetailEvent
    data object DeleteClick : DiaryDetailEvent
    data object DeleteConfirm : DiaryDetailEvent
}

sealed interface DiaryDetailEffect : ViewEffect {
    sealed interface Navigation : DiaryDetailEffect {
        data object Back : Navigation
        data class GoToEdit(val plantId: Int, val diaryId: Int) : Navigation
    }

    data object ShowDeleteConfirmDialog : DiaryDetailEffect
}
