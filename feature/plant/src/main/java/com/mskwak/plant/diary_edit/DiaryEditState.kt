package com.mskwak.plant.diary_edit

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import java.time.LocalDate

@Immutable
data class DiaryEditState(
    val plantId: Int = -1,
    val diaryId: Int? = null,
    val plantName: String = "",
    val imagePaths: List<String> = emptyList(),
    val content: String = "",
    val date: LocalDate = LocalDate.now(),
    val isSaveEnabled: Boolean = true
) : ViewState

sealed interface DiaryEditEvent : ViewEvent {
    data object OnBackClicked : DiaryEditEvent
    data class OnContentChanged(val content: String) : DiaryEditEvent
    data class OnDateChanged(val date: LocalDate) : DiaryEditEvent
    data class OnPictureAdded(val uri: Uri) : DiaryEditEvent
    data class OnPictureRemoved(val index: Int) : DiaryEditEvent
    data object OnPhotoClicked : DiaryEditEvent
    data object OnDateClicked : DiaryEditEvent
    data object OnSaveClicked : DiaryEditEvent
    data object OnConfirmBackClicked : DiaryEditEvent
}

sealed interface DiaryEditEffect : ViewEffect {
    sealed interface Navigation : DiaryEditEffect {
        data object Back : Navigation
        data object SaveComplete : Navigation
    }

    data object ShowPhotoPickerDialog : DiaryEditEffect
    data object ShowDatePicker : DiaryEditEffect
    data object ShowBackConfirmDialog : DiaryEditEffect
    data class ShowSnackbar(val message: String? = null, val messageResId: Int? = null) : DiaryEditEffect
}
