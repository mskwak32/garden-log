package com.mskwak.plant.diary_export.exported_list

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.domain.model.ExportedFileInfo

@Immutable
data class ExportedDiaryListState(
    val files: List<ExportedFileInfo> = emptyList(),
    val isLoading: Boolean = true
) : ViewState

sealed interface ExportedDiaryListEvent : ViewEvent {
    data object OnBackClicked : ExportedDiaryListEvent
    data class OnShareClicked(val uri: Uri) : ExportedDiaryListEvent
    data class OnDeleteClicked(val uri: Uri) : ExportedDiaryListEvent
    data class OnDeleteConfirmed(val uri: Uri) : ExportedDiaryListEvent
}

sealed interface ExportedDiaryListEffect : ViewEffect {
    data object NavigateBack : ExportedDiaryListEffect
    data class ShareFile(val uri: Uri) : ExportedDiaryListEffect
    data class ShowDeleteConfirmDialog(val uri: Uri) : ExportedDiaryListEffect
}
