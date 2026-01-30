package com.mskwak.plant.plant_edit

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Picture
import com.mskwak.domain.model.Plant
import com.mskwak.domain.useCase.picture.DeletePictureUseCase
import com.mskwak.domain.useCase.picture.SavePictureUseCase
import com.mskwak.domain.useCase.plant.AddPlantUseCase
import com.mskwak.domain.useCase.plant.GetPlantUseCase
import com.mskwak.domain.useCase.plant.UpdatePlantUseCase
import com.mskwak.plant.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class PlantEditViewModel @Inject constructor(
    private val application: Application,
    savedStateHandle: SavedStateHandle,
    private val getPlantUseCase: GetPlantUseCase,
    private val addPlantUseCase: AddPlantUseCase,
    private val updatePlantUseCase: UpdatePlantUseCase,
    private val savePictureUseCase: SavePictureUseCase,
    private val deletePictureUseCase: DeletePictureUseCase
) : BaseViewModel<PlantEditState, PlantEditEvent, PlantEditEffect>() {

    private val plantId: Int? = savedStateHandle.get<Int>("plantId")
    private var originalPicture: Picture? = null
    private var newPicture: Picture? = null

    init {
        plantId?.let { loadPlant(it) }
    }

    override fun setInitialState(): PlantEditState = PlantEditState(
        isEditMode = plantId != null
    )

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? PlantEditEvent ?: return
        when (event) {
            is PlantEditEvent.OnBackClicked -> {
                cleanupNewPicture()
                setEffect(PlantEditEffect.Navigation.Back)
            }

            is PlantEditEvent.OnNameChanged -> {
                setState { copy(plantName = event.name, isNameError = false) }
            }

            is PlantEditEvent.OnMemoChanged -> {
                setState { copy(memo = event.memo) }
            }

            is PlantEditEvent.OnCreatedDateChanged -> {
                setState { copy(createdDate = event.date) }
            }

            is PlantEditEvent.OnLastWateringDateChanged -> {
                setState { copy(lastWateringDate = event.date) }
            }

            is PlantEditEvent.OnWateringPeriodChanged -> {
                setState {
                    val alarmActive = if (event.period == 0) false else isWateringAlarmActive
                    copy(wateringPeriod = event.period, isWateringAlarmActive = alarmActive)
                }
            }

            is PlantEditEvent.OnWateringAlarmTimeChanged -> {
                setState { copy(wateringAlarmTime = event.time) }
            }

            is PlantEditEvent.OnWateringAlarmToggled -> {
                setState { copy(isWateringAlarmActive = event.isActive) }
            }

            is PlantEditEvent.OnPictureChanged -> {
                viewModelScope.launch {
                    val bytes = readBytesFromUri(event.uri) ?: return@launch
                    cleanupCameraCache()
                    // 이전에 새로 추가한 사진이 있으면 삭제
                    newPicture?.let { deletePictureUseCase(it) }
                    val picture = savePictureUseCase(bytes)
                    newPicture = picture
                    setState { copy(plantImagePath = picture.path) }
                }
            }

            is PlantEditEvent.OnPictureRemoved -> {
                CoroutineScope(Dispatchers.IO).launch {
                    newPicture?.let { deletePictureUseCase(it) }
                    newPicture = null
                }
                setState { copy(plantImagePath = null) }
            }

            is PlantEditEvent.OnPhotoClicked -> {
                setEffect(PlantEditEffect.ShowPhotoPickerDialog)
            }

            is PlantEditEvent.OnCreatedDateClicked -> {
                setEffect(PlantEditEffect.ShowCreatedDatePicker)
            }

            is PlantEditEvent.OnLastWateringDateClicked -> {
                setEffect(PlantEditEffect.ShowLastWateringDatePicker)
            }

            is PlantEditEvent.OnWateringPeriodClicked -> {
                setEffect(PlantEditEffect.ShowWateringPeriodDialog)
            }

            is PlantEditEvent.OnWateringAlarmTimeClicked -> {
                setEffect(PlantEditEffect.ShowWateringAlarmTimePicker)
            }

            is PlantEditEvent.OnSaveClicked -> {
                savePlant()
            }
        }
    }

    /**
     * 새로 추가한 사진을 정리. 취소 시 호출하여 원본만 유지.
     */
    private fun cleanupNewPicture() {
        val picture = newPicture ?: return
        newPicture = null
        CoroutineScope(Dispatchers.IO).launch {
            deletePictureUseCase(picture)
        }
    }

    private fun readBytesFromUri(uri: Uri): ByteArray? {
        return try {
            val bytes = application.contentResolver.openInputStream(uri)
                ?.use { it.readBytes() } ?: return null
            val rotation = getExifRotation(uri)
            if (rotation == 0) return bytes

            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return bytes
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(
                original, 0, 0, original.width, original.height, matrix, true
            )
            val output = java.io.ByteArrayOutputStream()
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, output)
            rotated.recycle()
            original.recycle()
            output.toByteArray()
        } catch (e: Exception) {
            Timber.e(e, "Failed to read bytes from uri")
            null
        }
    }

    private fun getExifRotation(uri: Uri): Int {
        return try {
            val exif = ExifInterface(
                application.contentResolver.openInputStream(uri) ?: return 0
            )
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to read exif orientation")
            0
        }
    }

    fun createCameraUri(): Uri {
        val cameraDir = application.cacheDir.resolve("camera").also { it.mkdirs() }
        val photoFile = java.io.File(cameraDir, "photo_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            application,
            "${application.packageName}.fileprovider",
            photoFile
        )
    }

    private fun cleanupCameraCache() {
        application.cacheDir.resolve("camera").listFiles()?.forEach { it.delete() }
    }

    private fun loadPlant(plantId: Int) {
        viewModelScope.launch {
            getPlantUseCase(plantId).collect { plant ->
                plant ?: return@collect
                originalPicture = plant.picture
                setState {
                    copy(
                        isEditMode = true,
                        plantImagePath = plant.picture?.path,
                        plantName = plant.name,
                        createdDate = plant.createdDate,
                        memo = plant.memo.orEmpty(),
                        lastWateringDate = plant.lastWateringDate,
                        wateringPeriod = plant.waterPeriod,
                        wateringAlarmTime = plant.wateringAlarm.time,
                        isWateringAlarmActive = plant.wateringAlarm.isActive
                    )
                }
            }
        }
    }

    private fun savePlant() {
        val state = viewState.value

        if (state.plantName.isBlank()) {
            setState { copy(isNameError = true) }
            return
        }

        viewModelScope.launch {
            try {
                // 새 사진으로 교체된 경우, 기존 원본 사진 삭제
                if (newPicture != null && originalPicture != null) {
                    deletePictureUseCase(originalPicture!!)
                }

                val currentPicture = if (newPicture != null) newPicture else originalPicture

                val plant = Plant(
                    id = plantId ?: 0,
                    name = state.plantName.trim(),
                    createdDate = state.createdDate,
                    waterPeriod = state.wateringPeriod,
                    lastWateringDate = state.lastWateringDate,
                    wateringAlarm = Alarm(
                        time = state.wateringAlarmTime,
                        isActive = if (state.wateringPeriod == 0) {
                            false
                        } else {
                            state.isWateringAlarmActive
                        }
                    ),
                    picture = currentPicture,
                    memo = state.memo.ifBlank { null }
                )

                if (plantId != null) {
                    updatePlantUseCase(plant)
                } else {
                    addPlantUseCase(plant)
                }

                setEffect(PlantEditEffect.Navigation.SaveComplete)
            } catch (e: Exception) {
                Timber.e(e, "Failed to save plant")
                setEffect(PlantEditEffect.ShowSnackbar(R.string.message_save_failed))
            }
        }
    }
}
