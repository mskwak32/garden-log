package com.mskwak.plant.plant_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.domain.useCase.plant.GetPlantsWithSortOrderUseCase
import com.mskwak.domain.useCase.watering.GetWateringDaysUseCase
import com.mskwak.domain.useCase.watering.WateringNowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val getPlantsWithSortOrderUseCase: GetPlantsWithSortOrderUseCase,
    private val getWateringDaysUseCase: GetWateringDaysUseCase,
    private val wateringNowUseCase: WateringNowUseCase
) : ViewModel() {
    private val _sortOrder = MutableStateFlow(PlantListSortOrder.CREATED_LATEST)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = _sortOrder.flatMapLatest { order ->
        getPlantsWithSortOrderUseCase(order).map { plants ->
            PlantListState(
                sortOrder = order,
                plants = plants.map { plant ->
                    plant.toPlantListItemUiModel {
                        getWateringDaysUseCase(it)
                    }
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlantListState()
    )
    private val _effect = Channel<PlantListEffect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: PlantListEvent) {
        when (event) {
            is PlantListEvent.OnPlantClicked -> {
                sendEffect(PlantListEffect.NavigateToPlantDetail(event.plantId))
            }

            is PlantListEvent.OnAddPlantClicked -> {
                sendEffect(PlantListEffect.NavigateToAddPlant)
            }

            is PlantListEvent.OnSortChanged -> {
                _sortOrder.value = event.sortOrder
            }

            is PlantListEvent.OnWateringClicked -> {
                waterPlant(event.plantId)
            }
        }
    }

    private fun sendEffect(effect: PlantListEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun waterPlant(plantId: Int) {
        viewModelScope.launch {
            wateringNowUseCase(plantId)
        }
    }
}