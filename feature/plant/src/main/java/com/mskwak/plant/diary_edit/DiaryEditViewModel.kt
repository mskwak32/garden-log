package com.mskwak.plant.diary_edit

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.Diary
import com.mskwak.domain.model.Picture
import com.mskwak.domain.useCase.diary.AddDiaryUseCase
import com.mskwak.domain.useCase.diary.GetDiaryUseCase
import com.mskwak.domain.useCase.diary.UpdateDiaryUseCase
import com.mskwak.domain.useCase.picture.DeletePictureUseCase
import com.mskwak.domain.useCase.picture.SavePictureUseCase
import com.mskwak.domain.useCase.plant.GetPlantNameUseCase
import com.mskwak.plant.R
import com.mskwak.plant.util.cleanupCameraCache
import com.mskwak.plant.util.createCameraUri
import com.mskwak.plant.util.readBytesFromUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DiaryEditViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val getPlantNameUseCase: GetPlantNameUseCase,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val addDiaryUseCase: AddDiaryUseCase,
    private val updateDiaryUseCase: UpdateDiaryUseCase,
    private val savePictureUseCase: SavePictureUseCase,
    private val deletePictureUseCase: DeletePictureUseCase
) : BaseViewModel<DiaryEditState, DiaryEditEvent, DiaryEditEffect>() {

    private var plantId: Int = savedStateHandle["plantId"] ?: -1
    private var diaryId: Int? = savedStateHandle["diaryId"]
    private var originalPictures: List<Picture> = emptyList()
    private var newPictures: MutableList<Picture> = mutableListOf()
    private var loadJob: Job? = null

    init {
        if (plantId != -1) {
            loadPlantName(plantId)
        }
        diaryId?.let { loadJob = loadDiary(it) }
    }

    override fun setInitialState(): DiaryEditState = DiaryEditState()

    private fun loadPlantName(id: Int) {
        viewModelScope.launch {
            val name = getPlantNameUseCase.getName(id)
            setState { copy(plantName = name.orEmpty()) }
        }
    }

    private fun loadDiary(id: Int): Job {
        return viewModelScope.launch {
            getDiaryUseCase(id).firstOrNull()?.let { diary ->
                originalPictures = diary.pictureList.orEmpty()
                setState {
                    copy(
                        content = diary.memo,
                        date = diary.createdDate,
                        imagePaths = originalPictures.map { it.path }
                    )
                }
            }
        }
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? DiaryEditEvent ?: return
        when (event) {
            is DiaryEditEvent.OnBackClicked -> {
                if (viewState.value.imagePaths.isNotEmpty() || viewState.value.content.isNotEmpty()) {
                    setEffect(DiaryEditEffect.ShowBackConfirmDialog)
                } else {
                    setEffect(DiaryEditEffect.Navigation.Back)
                }
            }

            is DiaryEditEvent.OnConfirmBackClicked -> {
                cleanupNewPictures()
                setEffect(DiaryEditEffect.Navigation.Back)
            }

            is DiaryEditEvent.OnContentChanged -> {
                setState { copy(content = event.content) }
            }

            is DiaryEditEvent.OnDateChanged -> {
                setState { copy(date = event.date) }
            }

            is DiaryEditEvent.OnPictureAdded -> {
                if (viewState.value.imagePaths.size >= 5) {
                    setEffect(DiaryEditEffect.ShowSnackbar(messageResId = R.string.message_no_more_picture))
                    return
                }
                viewModelScope.launch {
                    val bytes = readBytesFromUri(application, event.uri) ?: return@launch
                    cleanupCameraCache(application)
                    val picture = savePictureUseCase(bytes)
                    newPictures.add(picture)
                    setState { copy(imagePaths = imagePaths + picture.path) }
                }
            }

            is DiaryEditEvent.OnPictureRemoved -> {
                val path = viewState.value.imagePaths[event.index]
                val newPicture = newPictures.find { it.path == path }
                if (newPicture != null) {
                    newPictures.remove(newPicture)
                    CoroutineScope(Dispatchers.IO).launch {
                        deletePictureUseCase(newPicture)
                    }
                }
                setState { copy(imagePaths = imagePaths.toMutableList().apply { removeAt(event.index) }) }
            }

            is DiaryEditEvent.OnPhotoClicked -> {
                setEffect(DiaryEditEffect.ShowPhotoPickerDialog)
            }

            is DiaryEditEvent.OnDateClicked -> {
                setEffect(DiaryEditEffect.ShowDatePicker)
            }

            is DiaryEditEvent.OnSaveClicked -> {
                saveDiary()
            }

            else -> {}
        }
    }

    private fun cleanupNewPictures() {
        val pictures = newPictures.toList()
        newPictures.clear()
        CoroutineScope(Dispatchers.IO).launch {
            pictures.forEach { deletePictureUseCase(it) }
        }
    }

    fun createCameraUri() = createCameraUri(application)

    private fun saveDiary() {
        val state = viewState.value
        if (state.imagePaths.isEmpty() && state.content.isBlank()) {
            setEffect(DiaryEditEffect.ShowSnackbar(messageResId = R.string.message_input_picture_or_content))
            return
        }

        setState { copy(isSaveEnabled = false) }
        viewModelScope.launch {
            try {
                // Determine current pictures
                val currentPictures = state.imagePaths.mapNotNull { path ->
                    originalPictures.find { it.path == path } ?: newPictures.find { it.path == path }
                }

                // Delete original pictures that are no longer in the list
                val deletedOriginals = originalPictures.filter { orig ->
                    currentPictures.none { it.path == orig.path }
                }
                deletedOriginals.forEach { deletePictureUseCase(it) }

                val diary = Diary(
                    id = diaryId ?: 0,
                    plantId = plantId,
                    memo = state.content.trim(),
                    pictureList = currentPictures.ifEmpty { null },
                    createdDate = state.date
                )

                if (diaryId == null) {
                    addDiaryUseCase(diary)
                } else {
                    updateDiaryUseCase(diary)
                }
                setEffect(DiaryEditEffect.Navigation.SaveComplete)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save diary")
                setEffect(DiaryEditEffect.ShowSnackbar(messageResId = R.string.message_save_failed))
                setState { copy(isSaveEnabled = true) }
            }
        }
    }
}
