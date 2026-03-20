package com.mskwak.plant.plant_detail

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.plant.model.DiaryListItemUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate
import java.time.LocalTime

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
    val harvestMemoInput: String = ""
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
    data object OnHarvestConfirmed : PlantDetailEvent
    data object OnCancelHarvestClicked : PlantDetailEvent
}

sealed interface PlantDetailEffect : ViewEffect {
    sealed interface Navigation : PlantDetailEffect {
        data object Back : Navigation
        data object ToEditPlant : Navigation
        data object ToNewDiary : Navigation
        data class ToDiaryDetail(val diaryId: Int) : Navigation
        data object ToMoreDiaries : Navigation
    }

    data object ShowExactAlarmPermissionDialog : PlantDetailEffect
    data object ShowDeleteConfirmDialog : PlantDetailEffect
    data object ShowHarvestConfirmDialog : PlantDetailEffect
}