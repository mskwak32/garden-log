package com.mskwak.plant.diary_edit

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import java.time.LocalDate

@Immutable
data class DiaryEditState(
    val isEditMode: Boolean = false,
    val plantName: String = "",
    val picturePaths: List<String> = emptyList(),
    val diaryDate: LocalDate = LocalDate.now(),
    val memo: String = "",
    val isSaveEnabled: Boolean = true,
    val isWatered: Boolean = false
) : ViewState

sealed interface DiaryEditEvent : ViewEvent {
    data object OnBackClicked : DiaryEditEvent
    data object OnSaveClicked : DiaryEditEvent
    data class OnMemoChanged(val memo: String) : DiaryEditEvent
    data object OnDateClicked : DiaryEditEvent
    data class OnDateChanged(val date: LocalDate) : DiaryEditEvent
    data object OnAddPhotoClicked : DiaryEditEvent
    data class OnPicturesAdded(val uris: List<Uri>) : DiaryEditEvent
    data class OnPictureRemoved(val index: Int) : DiaryEditEvent
}

sealed interface DiaryEditEffect : ViewEffect {
    sealed interface Navigation : DiaryEditEffect {
        data object Back : Navigation
        data object SaveComplete : Navigation
    }

    data object ShowPhotoPickerDialog : DiaryEditEffect
    data object ShowDatePicker : DiaryEditEffect
    data object ShowDiscardConfirmDialog : DiaryEditEffect
    data class ShowSnackbar(val messageResId: Int) : DiaryEditEffect
}
