package com.mskwak.plant.diary_list

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.domain.model.DiaryListSortOrder
import com.mskwak.plant.model.DiaryListItemUiModel
import java.time.YearMonth

@Immutable
data class DiaryListState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val currentPlant: DiaryListPlantFilterUiModel? = null,
    val plantFilterList: List<DiaryListPlantFilterUiModel> = emptyList(),
    val sortOder: DiaryListSortOrder = DiaryListSortOrder.CREATED_LATEST,
    val diaries: List<DiaryListItemUiModel>
) : ViewState

sealed interface DiaryListEvent : ViewEvent {
    data class PlantFilterChanged(val plantFilter: DiaryListPlantFilterUiModel?) : DiaryListEvent
    data class SortOderChanged(val sortOder: DiaryListSortOrder) : DiaryListEvent
    data class MonthChanged(val yearMonth: YearMonth) : DiaryListEvent
    data object OnThisMonthClicked : DiaryListEvent
    data object OnNextMonthClicked : DiaryListEvent
    data object OnPreviousMonthClicked : DiaryListEvent
    data object SelectMonth : DiaryListEvent
    data class OnDiaryClicked(val diaryId: Int) : DiaryListEvent
}

sealed interface DiaryListEffect : ViewEffect {
    sealed interface Navigation : DiaryListEffect {
        data class GoToDiaryDetail(val diaryId: Long) : Navigation
    }

    data class ShowSelectMonth(val yearMonth: YearMonth) : DiaryListEffect
}