package com.mskwak.plant.diary_edit

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.Constants
import com.mskwak.domain.model.Diary
import com.mskwak.domain.model.Picture
import com.mskwak.domain.usecase.diary.AddDiaryUseCase
import com.mskwak.domain.usecase.diary.GetDiaryUseCase
import com.mskwak.domain.usecase.diary.UpdateDiaryUseCase
import com.mskwak.domain.usecase.picture.DeletePictureUseCase
import com.mskwak.domain.usecase.picture.SavePictureUseCase
import com.mskwak.domain.usecase.plant.GetPlantNameUseCase
import com.mskwak.domain.usecase.watering.GetWateringLogExistsUseCase
import com.mskwak.plant.R
import com.mskwak.plant.util.cleanupCameraCache
import com.mskwak.plant.util.readBytesFromUri
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

@HiltViewModel(assistedFactory = DiaryEditViewModel.Factory::class)
class DiaryEditViewModel @AssistedInject constructor(
    @Assisted navKey: DiaryEditNavKey,
    private val application: Application,
    private val addDiaryUseCase: AddDiaryUseCase,
    private val updateDiaryUseCase: UpdateDiaryUseCase,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val getPlantNameUseCase: GetPlantNameUseCase,
    private val savePictureUseCase: SavePictureUseCase,
    private val deletePictureUseCase: DeletePictureUseCase,
    private val getWateringLogExistsUseCase: GetWateringLogExistsUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<DiaryEditState, DiaryEditEvent, DiaryEditEffect>() {

    private val plantId: Int = navKey.plantId
    private val diaryId: Int? = navKey.diaryId
    private var originalPictures: List<Picture> = emptyList()
    private val newPictures: MutableList<Picture> = mutableListOf()
    private val removedOriginalPictures: MutableList<Picture> = mutableListOf()

    init {
        val screenName = if (navKey.diaryId != null) "diary_edit" else "diary_add"
        analyticsLogger.log(GardenEvent.ScreenView(screenName))
        loadPlantName()
        diaryId?.let { loadDiary(it) } ?: loadWateringStatus(LocalDate.now())
    }

    override fun setInitialState(): DiaryEditState = DiaryEditState(
        isEditMode = diaryId != null
    )

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? DiaryEditEvent ?: return
        when (event) {
            is DiaryEditEvent.OnBackClicked -> handleBack()
            is DiaryEditEvent.OnSaveClicked -> {
                setState { copy(isSaveEnabled = false) }
                saveDiary()
            }

            is DiaryEditEvent.OnMemoChanged -> setState { copy(memo = event.memo) }
            is DiaryEditEvent.OnDateClicked -> setEffect(DiaryEditEffect.ShowDatePicker)
            is DiaryEditEvent.OnDateChanged -> {
                setState { copy(diaryDate = event.date) }
                loadWateringStatus(event.date)
            }

            is DiaryEditEvent.OnAddPhotoClicked -> handleAddPhoto()
            is DiaryEditEvent.OnPicturesAdded -> addPictures(event)
            is DiaryEditEvent.OnPictureRemoved -> removePicture(event.index)
        }
    }

    fun createCameraUri() = com.mskwak.plant.util.createCameraUri(application)

    private fun loadWateringStatus(date: LocalDate) {
        viewModelScope.launch {
            val isWatered = getWateringLogExistsUseCase(plantId, date)
            setState { copy(isWatered = isWatered) }
        }
    }

    private fun handleBack() {
        val state = viewState.value
        val hasContent = state.picturePaths.isNotEmpty() || state.memo.isNotBlank()
        if (hasContent && !state.isEditMode) {
            setEffect(DiaryEditEffect.ShowDiscardConfirmDialog)
        } else {
            cleanupNewPictures()
            setEffect(DiaryEditEffect.Navigation.Back)
        }
    }

    fun confirmDiscard() {
        cleanupNewPictures()
        setEffect(DiaryEditEffect.Navigation.Back)
    }

    private fun handleAddPhoto() {
        val state = viewState.value
        if (state.picturePaths.size >= Constants.MAX_PICTURE_PER_DIARY) {
            setEffect(DiaryEditEffect.ShowSnackbar(R.string.message_no_more_picture))
        } else {
            setEffect(DiaryEditEffect.ShowPhotoPickerDialog)
        }
    }

    private fun addPictures(event: DiaryEditEvent.OnPicturesAdded) {
        viewModelScope.launch {
            var skipped = false
            for (uri in event.uris) {
                val currentSize = viewState.value.picturePaths.size
                if (currentSize >= Constants.MAX_PICTURE_PER_DIARY) {
                    skipped = true
                    break
                }
                val bytes = readBytesFromUri(application, uri) ?: continue
                cleanupCameraCache(application)
                val picture = savePictureUseCase(bytes)
                newPictures.add(picture)
                setState { copy(picturePaths = picturePaths + picture.path) }
            }
            if (skipped) {
                setEffect(DiaryEditEffect.ShowSnackbar(R.string.message_no_more_picture))
            }
        }
    }

    private fun removePicture(index: Int) {
        val state = viewState.value
        if (index !in state.picturePaths.indices) return
        val removedPath = state.picturePaths[index]

        // 새로 추가된 사진인지 확인
        val newPicture = newPictures.find { it.path == removedPath }
        if (newPicture != null) {
            newPictures.remove(newPicture)
            CoroutineScope(Dispatchers.IO).launch {
                deletePictureUseCase(newPicture)
            }
        } else {
            // 원본 사진 삭제 추적
            val originalPicture = originalPictures.find { it.path == removedPath }
            if (originalPicture != null) {
                removedOriginalPictures.add(originalPicture)
            }
        }

        setState { copy(picturePaths = picturePaths.toMutableList().apply { removeAt(index) }) }
    }

    private fun cleanupNewPictures() {
        if (newPictures.isEmpty()) return
        val pictures = newPictures.toList()
        newPictures.clear()
        CoroutineScope(Dispatchers.IO).launch {
            pictures.forEach { deletePictureUseCase(it) }
        }
    }

    private fun loadPlantName() {
        viewModelScope.launch {
            val name = getPlantNameUseCase.getName(plantId) ?: return@launch
            setState { copy(plantName = name) }
        }
    }

    private fun loadDiary(diaryId: Int) {
        viewModelScope.launch {
            getDiaryUseCase(diaryId).collect { diary ->
                originalPictures = diary.pictureList ?: emptyList()
                val isWatered = getWateringLogExistsUseCase(plantId, diary.createdDate)
                setState {
                    copy(
                        isEditMode = true,
                        memo = diary.memo,
                        diaryDate = diary.createdDate,
                        picturePaths = originalPictures.map { it.path },
                        isWatered = isWatered
                    )
                }
            }
        }
    }

    private fun saveDiary() {
        val state = viewState.value

        if (state.memo.isBlank() && state.picturePaths.isEmpty()) {
            setState { copy(isSaveEnabled = true) }
            setEffect(DiaryEditEffect.ShowSnackbar(R.string.message_input_picture_or_content))
            return
        }

        viewModelScope.launch {
            try {
                // 삭제된 원본 사진 파일 삭제
                removedOriginalPictures.forEach { deletePictureUseCase(it) }

                val currentPictures = buildCurrentPictureList()

                val diary = Diary(
                    id = diaryId ?: 0,
                    plantId = plantId,
                    memo = state.memo,
                    pictureList = currentPictures.ifEmpty { null },
                    createdDate = state.diaryDate
                )

                if (diaryId != null) {
                    updateDiaryUseCase(diary)
                } else {
                    addDiaryUseCase(diary)
                    analyticsLogger.log(GardenEvent.AddDiary)
                }

                setEffect(DiaryEditEffect.Navigation.SaveComplete)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save diary")
                setEffect(DiaryEditEffect.ShowSnackbar(R.string.message_save_failed))
            } finally {
                setState { copy(isSaveEnabled = true) }
            }
        }
    }

    private fun buildCurrentPictureList(): List<Picture> {
        val state = viewState.value
        return state.picturePaths.mapNotNull { path ->
            newPictures.find { it.path == path }
                ?: originalPictures.find { it.path == path }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: DiaryEditNavKey): DiaryEditViewModel
    }
}
