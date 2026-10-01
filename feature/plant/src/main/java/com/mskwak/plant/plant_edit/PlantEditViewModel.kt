package com.mskwak.plant.plant_edit

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Picture
import com.mskwak.domain.model.Plant
import com.mskwak.domain.usecase.picture.DeletePictureUseCase
import com.mskwak.domain.usecase.picture.SavePictureUseCase
import com.mskwak.domain.usecase.plant.AddPlantUseCase
import com.mskwak.domain.usecase.plant.GetPlantUseCase
import com.mskwak.domain.usecase.plant.UpdatePlantUseCase
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import com.mskwak.plant.R
import com.mskwak.plant.util.canScheduleExactAlarms
import com.mskwak.plant.util.cleanupCameraCache
import com.mskwak.plant.util.createCameraUri
import com.mskwak.plant.util.readBytesFromUri
import com.mskwak.plant.util.wateringErrorResource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = PlantEditViewModel.Factory::class)
class PlantEditViewModel @AssistedInject constructor(
    @Assisted navKey: PlantEditNavKey,
    private val application: Application,
    private val getPlantUseCase: GetPlantUseCase,
    private val addPlantUseCase: AddPlantUseCase,
    private val updatePlantUseCase: UpdatePlantUseCase,
    private val savePictureUseCase: SavePictureUseCase,
    private val deletePictureUseCase: DeletePictureUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<PlantEditState, PlantEditEvent, PlantEditEffect>() {

    private var plantId: Int? = navKey.plantId
    private var originalPlant: Plant? = null
    private var originalPicture: Picture? = null
    private var newPicture: Picture? = null
    private var loadJob: Job? = null

    init {
        val screenName = if (navKey.plantId != null) "plant_edit" else "plant_add"
        // 기존 식물은 원본을 읽기 전 기본값으로 저장하지 못하게 한다.
        setState {
            copy(
                isEditMode = navKey.plantId != null,
                isSaveEnabled = navKey.plantId == null
            )
        }
        analyticsLogger.log(GardenEvent.ScreenView(screenName))
        plantId?.let { loadJob = loadPlant(it) }
    }

    override fun setInitialState(): PlantEditState = PlantEditState(isSaveEnabled = false)

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
                if (event.isActive && !canScheduleExactAlarms(application)) {
                    setEffect(PlantEditEffect.ShowExactAlarmPermissionDialog)
                } else {
                    setState { copy(isWateringAlarmActive = event.isActive) }
                }
            }

            is PlantEditEvent.OnPictureChanged -> {
                viewModelScope.launch {
                    val bytes = readBytesFromUri(application, event.uri) ?: return@launch
                    cleanupCameraCache(application)
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
                if (!viewState.value.isSaveEnabled) return
                setState { copy(isSaveEnabled = false) }
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

    fun createCameraUri(): Uri = createCameraUri(application)

    @AssistedFactory
    interface Factory {
        fun create(navKey: PlantEditNavKey): PlantEditViewModel
    }

    private fun loadPlant(plantId: Int): Job {
        return viewModelScope.launch {
            try {
                // 입력 도중 DB 갱신으로 편집 내용을 덮어쓰지 않도록 최초 값만 읽는다.
                val plant = getPlantUseCase(plantId).first { it != null } ?: return@launch
                originalPlant = plant
                originalPicture = plant.picture
                setState {
                    copy(
                        isEditMode = true,
                        isSaveEnabled = true,
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to load plant")
                setEffect(PlantEditEffect.ShowSnackbar(R.string.message_plant_load_failed))
            }
        }
    }

    private fun savePlant() {
        val state = viewState.value

        if (state.plantName.isBlank()) {
            setState { copy(isNameError = true, isSaveEnabled = true) }
            return
        }

        viewModelScope.launch {
            try {
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
                    memo = state.memo.ifBlank { null },
                    harvestDate = originalPlant?.harvestDate,
                    harvestMemo = originalPlant?.harvestMemo
                )

                // 날짜와 이력의 일관성은 Repository 저장 경계에서 검증한다.
                val result = if (plantId != null) {
                    val saved = updatePlantUseCase(plant)
                    analyticsLogger.log(GardenEvent.UpdatePlant)
                    saved
                } else {
                    val saved = addPlantUseCase(plant)
                    analyticsLogger.log(
                        GardenEvent.AddPlant(
                            wateringInterval = state.wateringPeriod,
                            alarmEnabled = state.isWateringAlarmActive
                        )
                    )
                    saved
                }
                // 알람 예약이 실패해도 저장된 식물을 다시 등록하도록 유도하지 않는다.
                if (result == WateringUpdateResult.ALARM_FAILED) {
                    Toast.makeText(application, R.string.message_watering_alarm_failed, Toast.LENGTH_LONG).show()
                }

                val replacedPicture = if (newPicture != null) originalPicture else null
                // 저장한 사진은 식물 소유로 넘겨 취소 시 정리 대상에서 제외한다.
                newPicture = null
                if (replacedPicture != null) {
                    try {
                        deletePictureUseCase(replacedPicture)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // 파일 정리 실패를 이미 완료된 DB 저장 실패로 안내하지 않는다.
                        Timber.e(e, "Failed to clean up replaced picture")
                    }
                }
                setEffect(PlantEditEffect.Navigation.SaveComplete)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to save plant")
                setEffect(PlantEditEffect.ShowSnackbar(e.wateringErrorResource()))
            } finally {
                setState { copy(isSaveEnabled = true) }
            }
        }
    }
}
