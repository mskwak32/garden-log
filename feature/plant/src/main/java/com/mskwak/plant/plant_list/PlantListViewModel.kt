package com.mskwak.plant.plant_list

import androidx.lifecycle.viewModelScope
import com.mskwak.analytics.AnalyticsLogger
import com.mskwak.analytics.GardenEvent
import com.mskwak.analytics.WateringSource
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.base.BaseViewModel
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.domain.usecase.plant.GetPlantsWithSortOrderUseCase
import com.mskwak.domain.usecase.watering.GetWateringDaysUseCase
import com.mskwak.domain.usecase.watering.WateringNowUseCase
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import com.mskwak.plant.R
import com.mskwak.plant.model.toPlantListItemUiModel
import com.mskwak.plant.util.wateringErrorResource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@HiltViewModel
class PlantListViewModel @Inject constructor(
    private val getPlantsWithSortOrderUseCase: GetPlantsWithSortOrderUseCase,
    private val getWateringDaysUseCase: GetWateringDaysUseCase,
    private val wateringNowUseCase: WateringNowUseCase,
    private val analyticsLogger: AnalyticsLogger
) : BaseViewModel<PlantListState, PlantListEvent, PlantListEffect>() {
    private val pendingWatering = mutableSetOf<Int>()
    private val _sortOrder = MutableStateFlow(PlantListSortOrder.CREATED_LATEST)
    private val _selectedTab = MutableStateFlow(PlantListTab.MY_GARDEN)

    init {
        analyticsLogger.log(GardenEvent.ScreenView("plant_list"))
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
                analyticsLogger.log(GardenEvent.WateringClick(WateringSource.LIST))
                waterPlant(event.plantId)
            }

            is PlantListEvent.OnTabChanged -> {
                _selectedTab.value = event.tab
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePlants() {
        viewModelScope.launch {
            combine(
                _sortOrder.flatMapLatest { getPlantsWithSortOrderUseCase(it) },
                _sortOrder,
                _selectedTab
            ) { plants, order, tab ->
                Triple(plants, order, tab)
            }.collect { (plants, order, tab) ->
                val allUiModels = plants.map { plant ->
                    plant.toPlantListItemUiModel {
                        getWateringDaysUseCase(it)
                    }
                }
                // 선택된 탭에 따라 수확 여부로 필터링
                val filtered =
                    allUiModels.filter { it.isHarvested == (tab == PlantListTab.HARVESTED) }
                setState { copy(plants = filtered, sortOrder = order, selectedTab = tab) }
            }
        }
    }

    private fun waterPlant(plantId: Int) {
        // 같은 식물의 연속 클릭만 차단하고 다른 식물은 별도로 처리한다.
        if (!pendingWatering.add(plantId)) return
        viewModelScope.launch {
            try {
                // 기록 저장 완료와 알람 예약 실패를 분리해 안내한다.
                if (wateringNowUseCase(plantId) == WateringUpdateResult.ALARM_FAILED) {
                    setEffect(PlantListEffect.ShowSnackbar(R.string.message_watering_alarm_failed))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setEffect(PlantListEffect.ShowSnackbar(e.wateringErrorResource()))
            } finally {
                pendingWatering.remove(plantId)
            }
        }
    }
}
