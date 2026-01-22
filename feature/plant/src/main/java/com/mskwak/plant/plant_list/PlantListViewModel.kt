package com.mskwak.plant.plant_list

import androidx.lifecycle.viewModelScope
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.domain.useCase.plant.GetPlantsWithSortOrderUseCase
import com.mskwak.domain.useCase.watering.GetWateringDaysUseCase
import com.mskwak.domain.useCase.watering.WateringNowUseCase
import com.mskwak.plant.model.toPlantListItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val getPlantsWithSortOrderUseCase: GetPlantsWithSortOrderUseCase,
    private val getWateringDaysUseCase: GetWateringDaysUseCase,
    private val wateringNowUseCase: WateringNowUseCase
) : BaseViewModel<PlantListState, PlantListEvent, PlantListEffect>() {
    private val _sortOrder = MutableStateFlow(PlantListSortOrder.CREATED_LATEST)

    init {
        observePlants()
    }

    override fun setInitialState(): PlantListState = PlantListState()

    override fun handleEvents(viewEvent: ViewEvent) {
        val event = viewEvent as? PlantListEvent ?: return

        when (event) {
            is PlantListEvent.OnPlantClicked -> {
                setEffect(PlantListEffect.Navigation.ToPlantDetail(event.plantId))
            }

            is PlantListEvent.AddPlant -> {
                setEffect(PlantListEffect.Navigation.ToAddPlant)
            }

            is PlantListEvent.OnSortChanged -> {
                _sortOrder.value = event.sortOrder
            }

            is PlantListEvent.Watering -> {
                waterPlant(event.plantId)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlants() {
        viewModelScope.launch {
            _sortOrder.flatMapLatest { order ->
                setState { copy(sortOrder = order) }
                getPlantsWithSortOrderUseCase(order)
            }.collect { plants ->
                val uiModels = plants.map { plant ->
                    plant.toPlantListItemUiModel {
                        getWateringDaysUseCase(it)
                    }
                }
                setState { copy(plants = uiModels) }
            }
        }
    }

    private fun waterPlant(plantId: Int) {
        viewModelScope.launch {
            wateringNowUseCase(plantId)
        }
    }
}