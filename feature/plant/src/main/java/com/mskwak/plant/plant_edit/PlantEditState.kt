package com.mskwak.plant.plant_edit

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class PlantEditState(
    val isEditMode: Boolean = false,
    val plantImagePath: String? = null,
    val plantName: String = "",
    val createdDate: LocalDate = LocalDate.now(),
    val memo: String = "",
    val lastWateringDate: LocalDate = LocalDate.now(),
    val wateringPeriod: Int = 0,
    val wateringAlarmTime: LocalTime = LocalTime.of(9, 0),
    val isWateringAlarmActive: Boolean = false,
    val isNameError: Boolean = false,
    val isSaveEnabled: Boolean = true
) : ViewState

sealed interface PlantEditEvent : ViewEvent {
    data object OnBackClicked : PlantEditEvent
    data class OnNameChanged(val name: String) : PlantEditEvent
    data class OnMemoChanged(val memo: String) : PlantEditEvent
    data class OnCreatedDateChanged(val date: LocalDate) : PlantEditEvent
    data class OnLastWateringDateChanged(val date: LocalDate) : PlantEditEvent
    data class OnWateringPeriodChanged(val period: Int) : PlantEditEvent
    data class OnWateringAlarmTimeChanged(val time: LocalTime) : PlantEditEvent
    data class OnWateringAlarmToggled(val isActive: Boolean) : PlantEditEvent
    data class OnPictureChanged(val uri: Uri) : PlantEditEvent
    data object OnPictureRemoved : PlantEditEvent
    data object OnPhotoClicked : PlantEditEvent
    data object OnCreatedDateClicked : PlantEditEvent
    data object OnLastWateringDateClicked : PlantEditEvent
    data object OnWateringPeriodClicked : PlantEditEvent
    data object OnWateringAlarmTimeClicked : PlantEditEvent
    data object OnSaveClicked : PlantEditEvent
}

sealed interface PlantEditEffect : ViewEffect {
    sealed interface Navigation : PlantEditEffect {
        data object Back : Navigation
        data object SaveComplete : Navigation
    }

    data object ShowPhotoPickerDialog : PlantEditEffect
    data object ShowCreatedDatePicker : PlantEditEffect
    data object ShowLastWateringDatePicker : PlantEditEffect
    data object ShowWateringPeriodDialog : PlantEditEffect
    data object ShowWateringAlarmTimePicker : PlantEditEffect
    data object ShowExactAlarmPermissionDialog : PlantEditEffect
    data class ShowSnackbar(val messageResId: Int) : PlantEditEffect
}