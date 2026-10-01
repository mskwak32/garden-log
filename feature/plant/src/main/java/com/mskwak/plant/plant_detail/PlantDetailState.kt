package com.mskwak.plant.plant_detail

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.plant.model.DiaryListItemUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Immutable
data class PlantDetailState(
    val plantImagePath: String? = null,
    val plantName: String = "",
    val createdAt: LocalDate? = null,
    val dDays: Int = 0,
    val wateringStatus: WateringStatus = WateringStatus.UPCOMING,
    val lastWateringDate: LocalDate? = null,
    val wateringAlarmTime: LocalTime? = null,
    val isWateringActive: Boolean = false,
    val memo: String? = null,
    val diaries: List<DiaryListItemUiModel> = emptyList(),
    val harvestDate: LocalDate? = null,
    val harvestMemo: String? = null,
    val isHarvestSectionExpanded: Boolean = false,
    val harvestMemoInput: String = "",
    val isWateringDateDialogOpen: Boolean = false,
    val wateringMonth: YearMonth = YearMonth.now(),
    val selectedWateringDate: LocalDate = LocalDate.now(),
    // 모든 이력을 보관해 월 이동과 범위 밖 기존 기록 조회에 함께 사용한다.
    val wateringDates: Set<LocalDate> = emptySet(),
    // 조회 완료 전의 빈 집합은 실제 기록 없음 상태와 구분한다.
    val isWateringLoading: Boolean = true,
    val isWateringSaving: Boolean = false,
    val wateringError: Int? = null,
    val today: LocalDate = LocalDate.now()
) : ViewState {
    val isHarvested: Boolean get() = harvestDate != null
}

sealed interface PlantDetailEvent : ViewEvent {
    data object OnBackClicked : PlantDetailEvent
    data object OnEditPlantClicked : PlantDetailEvent
    data object OnDeletePlantClicked : PlantDetailEvent
    data object OnDeleteConfirmClicked : PlantDetailEvent
    data class ToggleWateringAlarmActive(val isActive: Boolean) : PlantDetailEvent
    data object OnWateringClicked : PlantDetailEvent
    data class OnDiaryClicked(val diaryId: Int) : PlantDetailEvent
    data object OnNewDiaryClicked : PlantDetailEvent
    data object OnMoreDiaryClicked : PlantDetailEvent
    data object OnHarvestSectionToggled : PlantDetailEvent
    data class OnHarvestMemoChanged(val memo: String) : PlantDetailEvent
    data object OnHarvestClicked : PlantDetailEvent
    data class OnHarvestConfirmed(val date: LocalDate) : PlantDetailEvent
    data object OnCancelHarvestClicked : PlantDetailEvent
    data object OnCancelWateringClicked : PlantDetailEvent
    data object OnWateringCalendarClicked : PlantDetailEvent
    data object OnWateringCalendarClosed : PlantDetailEvent
    data class OnWateringMonthChanged(val month: YearMonth) : PlantDetailEvent
    data class OnWateringDateSelected(val date: LocalDate) : PlantDetailEvent
    data object OnWateringDateSaved : PlantDetailEvent
    data object OnWateringRetry : PlantDetailEvent
    data object OnExportClicked : PlantDetailEvent
}

sealed interface PlantDetailEffect : ViewEffect {
    sealed interface Navigation : PlantDetailEffect {
        data object Back : Navigation
        data object ToEditPlant : Navigation
        data object ToNewDiary : Navigation
        data class ToDiaryDetail(val diaryId: Int) : Navigation
        data object ToMoreDiaries : Navigation
        data object ToExportDiary : Navigation
    }

    data class ShowSnackbar(val message: Int) : PlantDetailEffect
    data object ShowExactAlarmPermissionDialog : PlantDetailEffect
    data object ShowDeleteConfirmDialog : PlantDetailEffect
    data object ShowHarvestConfirmDialog : PlantDetailEffect
}