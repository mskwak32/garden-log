package com.mskwak.plant.plant_detail

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.useCase.diary.GetDiariesByPlantIdUseCase
import com.mskwak.domain.useCase.plant.DeletePlantUseCase
import com.mskwak.domain.useCase.plant.GetPlantUseCase
import com.mskwak.domain.useCase.plant.HarvestPlantUseCase
import com.mskwak.domain.useCase.watering.GetWateringDaysUseCase
import com.mskwak.domain.useCase.watering.UpdateWateringAlarmActivationUseCase
import com.mskwak.domain.useCase.watering.WateringNowUseCase
import com.mskwak.plant.model.toDiaryListItemUiModel
import com.mskwak.plant.model.toPlantListItemUiModel
import com.mskwak.plant.util.canScheduleExactAlarms
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PlantDetailViewModel.Factory::class)
class PlantDetailViewModel @AssistedInject constructor(
    @Assisted navKey: PlantDetailNavKey,
    private val application: Application,
    private val getPlantUseCase: GetPlantUseCase,
    private val getWateringDaysUseCase: GetWateringDaysUseCase,
    private val getDiariesByPlantIdUseCase: GetDiariesByPlantIdUseCase,
    private val wateringNowUseCase: WateringNowUseCase,
    private val updateWateringAlarmActivationUseCase: UpdateWateringAlarmActivationUseCase,
    private val deletePlantUseCase: DeletePlantUseCase,
    private val plantRepository: PlantRepository,
    private val harvestPlantUseCase: HarvestPlantUseCase
) : BaseViewModel<PlantDetailState, PlantDetailEvent, PlantDetailEffect>() {

    private val plantId: Int = navKey.plantId
    private var observeJob: Job? = null

    init {
        observeJob = observePlant()
    }

    override fun setInitialState(): PlantDetailState = PlantDetailState()

    private fun observePlant(): Job {
        return combine(
            getPlantUseCase(plantId).filterNotNull(),
            getDiariesByPlantIdUseCase(plantId)
        ) { plant, diaries ->
            plant to diaries
        }.onEach { (plant, diaries) ->
            val uiModel = plant.toPlantListItemUiModel { getWateringDaysUseCase(it) }
            setState {
                copy(
                    plantImagePath = uiModel.imagePath,
                    plantName = uiModel.name,
                    createdAt = uiModel.createdAt,
                    dDays = uiModel.dDay,
                    wateringStatus = uiModel.status,
                    lastWateringDate = plant.lastWateringDate,
                    wateringAlarmTime = if (plant.waterPeriod == 0) null else plant.wateringAlarm.time,
                    isWateringActive = plant.wateringAlarm.isActive,
                    memo = plant.memo,
                    diaries = diaries.map { it.toDiaryListItemUiModel() },
                    harvestDate = plant.harvestDate,
                    harvestMemo = plant.harvestMemo
                )
            }
        }
            .launchIn(viewModelScope)
    }

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? PlantDetailEvent ?: return

        when (event) {
            is PlantDetailEvent.OnBackClicked -> {
                setEffect(PlantDetailEffect.Navigation.Back)
            }

            is PlantDetailEvent.OnEditPlantClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToEditPlant)
            }

            is PlantDetailEvent.OnDeletePlantClicked -> {
                setEffect(PlantDetailEffect.ShowDeleteConfirmDialog)
            }

            is PlantDetailEvent.OnDeleteConfirmClicked -> {
                deletePlant()
            }

            is PlantDetailEvent.ToggleWateringAlarmActive -> {
                toggleWateringAlarm(event.isActive)
            }

            is PlantDetailEvent.OnWateringClicked -> {
                waterPlant()
            }

            is PlantDetailEvent.OnDiaryClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToDiaryDetail(event.diaryId))
            }

            is PlantDetailEvent.OnNewDiaryClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToNewDiary)
            }

            is PlantDetailEvent.OnMoreDiaryClicked -> {
                setEffect(PlantDetailEffect.Navigation.ToMoreDiaries)
            }

            is PlantDetailEvent.OnHarvestSectionToggled -> {
                setState { copy(isHarvestSectionExpanded = !isHarvestSectionExpanded) }
            }

            is PlantDetailEvent.OnHarvestMemoChanged -> {
                setState { copy(harvestMemoInput = event.memo) }
            }

            is PlantDetailEvent.OnHarvestClicked -> {
                setEffect(PlantDetailEffect.ShowHarvestConfirmDialog)
            }

            is PlantDetailEvent.OnHarvestConfirmed -> {
                harvestPlant()
            }

            is PlantDetailEvent.OnCancelHarvestClicked -> {
                cancelHarvest()
            }
        }
    }

    private fun toggleWateringAlarm(isActive: Boolean) {
        if (isActive && !canScheduleExactAlarms(application)) {
            setEffect(PlantDetailEffect.ShowExactAlarmPermissionDialog)
            return
        }

        viewModelScope.launch {
            updateWateringAlarmActivationUseCase(plantId, isActive)
        }
    }

    private fun waterPlant() {
        viewModelScope.launch {
            wateringNowUseCase(plantId)
        }
    }

    private fun deletePlant() {
        viewModelScope.launch {
            val plant = plantRepository.getPlant(plantId) ?: return@launch
            deletePlantUseCase(plant)
            setEffect(PlantDetailEffect.Navigation.Back)
        }
    }

    private fun harvestPlant() {
        viewModelScope.launch {
            harvestPlantUseCase.harvest(
                plantId = plantId,
                harvestMemo = viewState.value.harvestMemoInput.ifBlank { null }
            )
            setState { copy(isHarvestSectionExpanded = false, harvestMemoInput = "") }
        }
    }

    private fun cancelHarvest() {
        viewModelScope.launch {
            harvestPlantUseCase.cancelHarvest(plantId)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: PlantDetailNavKey): PlantDetailViewModel
    }
}