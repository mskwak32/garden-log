package com.mskwak.plant.plant_list

import androidx.compose.runtime.Immutable
import com.mskwak.common_ui.ViewEffect
import com.mskwak.common_ui.ViewEvent
import com.mskwak.common_ui.ViewState
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.plant.model.PlantListItemUiModel

@Immutable
data class PlantListState(
    val sortOrder: PlantListSortOrder = PlantListSortOrder.CREATED_LATEST,
    val plants: List<PlantListItemUiModel> = emptyList()
) : ViewState

sealed interface PlantListEvent : ViewEvent {
    data class OnSortChanged(val sortOrder: PlantListSortOrder) : PlantListEvent
    data object AddPlant : PlantListEvent
    data class OnPlantClicked(val plantId: Int) : PlantListEvent
    data class Watering(val plantId: Int) : PlantListEvent
}

sealed interface PlantListEffect : ViewEffect {
    sealed interface Navigation : PlantListEffect {
        data class ToPlantDetail(val plantId: Int) : Navigation
        data object ToAddPlant : Navigation
    }
}
