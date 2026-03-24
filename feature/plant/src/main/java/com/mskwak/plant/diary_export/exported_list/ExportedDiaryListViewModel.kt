package com.mskwak.plant.diary_export.exported_list

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.usecase.export.DeleteExportedFileUseCase
import com.mskwak.domain.usecase.export.GetExportedFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExportedDiaryListViewModel @Inject constructor(
    private val getExportedFilesUseCase: GetExportedFilesUseCase,
    private val deleteExportedFileUseCase: DeleteExportedFileUseCase
) : BaseViewModel<ExportedDiaryListState, ExportedDiaryListEvent, ExportedDiaryListEffect>() {

    init {
        loadFiles()
    }

    override fun setInitialState() = ExportedDiaryListState()

    private fun loadFiles() {
        viewModelScope.launch {
            val files = getExportedFilesUseCase()
            setState { copy(files = files, isLoading = false) }
        }
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? ExportedDiaryListEvent ?: return

        when (event) {
            is ExportedDiaryListEvent.OnBackClicked -> {
                setEffect(ExportedDiaryListEffect.NavigateBack)
            }

            is ExportedDiaryListEvent.OnOpenClicked -> {
                setEffect(ExportedDiaryListEffect.OpenFile(event.uri))
            }

            is ExportedDiaryListEvent.OnShareClicked -> {
                setEffect(ExportedDiaryListEffect.ShareFile(event.uri))
            }

            is ExportedDiaryListEvent.OnDeleteClicked -> {
                setEffect(ExportedDiaryListEffect.ShowDeleteConfirmDialog(event.uri))
            }

            is ExportedDiaryListEvent.OnDeleteConfirmed -> {
                deleteFile(event.uri)
            }
        }
    }

    private fun deleteFile(uri: Uri) {
        viewModelScope.launch {
            try {
                deleteExportedFileUseCase(uri.toString())
                loadFiles()
            } catch (e: Exception) {
                Timber.e(e, "PDF 삭제 실패: $uri")
            }
        }
    }
}
