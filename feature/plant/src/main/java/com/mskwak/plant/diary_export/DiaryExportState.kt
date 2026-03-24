package com.mskwak.plant.diary_export

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import java.time.LocalDate

@Immutable
data class DiaryExportState(
    val plantName: String = "",
    val includeFirstPage: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val diaryDateRange: Pair<LocalDate, LocalDate>? = null,
    val isExporting: Boolean = false
) : ViewState

sealed interface DiaryExportEvent : ViewEvent {
    data object OnBackClicked : DiaryExportEvent
    data class OnIncludeFirstPageChanged(val include: Boolean) : DiaryExportEvent
    data class OnStartDateChanged(val date: LocalDate) : DiaryExportEvent
    data class OnEndDateChanged(val date: LocalDate) : DiaryExportEvent
    data object OnExportClicked : DiaryExportEvent
}

sealed interface DiaryExportEffect : ViewEffect {
    data object NavigateBack : DiaryExportEffect
    data object ShowExportSuccess : DiaryExportEffect
    data object ShowExportFail : DiaryExportEffect
    data object ShowNoDiaryInRange : DiaryExportEffect
}
