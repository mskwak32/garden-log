package com.mskwak.plant.diary_more

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.domain.model.DiaryListSortOrder

@Immutable
data class DiaryMoreState(
    val plantName: String = "",
    val sortOrder: DiaryListSortOrder = DiaryListSortOrder.CREATED_LATEST,
    val diaries: List<DiaryMoreListItem> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true
) : ViewState

sealed interface DiaryMoreEvent : ViewEvent {
    data object BackClick : DiaryMoreEvent
    data class OnDiaryClicked(val diaryId: Int) : DiaryMoreEvent
    data class SortOrderChanged(val sortOrder: DiaryListSortOrder) : DiaryMoreEvent
    data object LoadMore : DiaryMoreEvent
}

sealed interface DiaryMoreEffect : ViewEffect {
    sealed interface Navigation : DiaryMoreEffect {
        data object Back : Navigation
        data class ToDiaryDetail(val diaryId: Int) : Navigation
    }
}
