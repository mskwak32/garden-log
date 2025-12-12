package com.mskwak.plant.plant_list

import androidx.compose.runtime.Immutable
import com.mskwak.domain.model.PlantListSortOrder

@Immutable
data class PlantListState(
    val sortOrder: PlantListSortOrder = PlantListSortOrder.CREATED_LATEST,
    val plants: List<PlantListItemUiModel> = emptyList()
)

sealed interface PlantListEvent {
    data class OnSortChanged(val sortOrder: PlantListSortOrder) : PlantListEvent
    data object OnAddPlantClicked : PlantListEvent
    data class OnPlantClicked(val plantId: Int) : PlantListEvent
    data class OnWateringClicked(val plantId: Int) : PlantListEvent
}

sealed interface PlantListEffect {
    data class NavigateToPlantDetail(val plantId: Int) : PlantListEffect
    data object NavigateToAddPlant : PlantListEffect
}
