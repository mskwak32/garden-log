package com.mskwak.plant.plant_detail

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.plant.model.DiaryListItemUiModel
import com.mskwak.plant.model.WateringStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * @param hasWateringPeriod 물주기 설정여부
 */
@Immutable
data class PlantDetailState(
    val plantImagePath: String? = null,
    val plantName: String = "",
    val createdAt: LocalDate? = null,
    val dDays: Int = 0,
    val hasWateringPeriod: Boolean = false,
    val wateringStatus: WateringStatus = WateringStatus.UPCOMING,
    val lastWateringDate: LocalDate? = null,
    val wateringAlarmTime: LocalTime? = null,
    val isWateringActive: Boolean = false,
    val memo: String? = null,
    val diaries: List<DiaryListItemUiModel> = emptyList()
) : ViewState

sealed interface PlantDetailEvent : ViewEvent {
    data object OnBackClicked : PlantDetailEvent
    data object EditPlant : PlantDetailEvent
    data object DeletePlant : PlantDetailEvent
    data object DeleteConfirmClicked : PlantDetailEvent
    data class ToggleWateringAlarmActive(val isActive: Boolean) : PlantDetailEvent
    data object OnWateringClicked : PlantDetailEvent
    data class OnDiaryClicked(val diaryId: Int) : PlantDetailEvent
    data object NewDiary : PlantDetailEvent
    data object ShowMoreDiary : PlantDetailEvent
}

sealed interface PlantDetailEffect : ViewEffect {
    sealed interface Navigation : PlantDetailEffect {
        data object Back : Navigation
        data object ToEditPlant : Navigation
        data object ToNewDiary : Navigation
        data object ToDiaryDetail : Navigation
        data object ToMoreDiaries : Navigation
    }

    data object ShowExactAlarmPermissionDialog : PlantDetailEffect
}