package com.mskwak.domain.useCase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.PlantListSortOrder
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.useCase.watering.GetRemainWateringDateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class GetPlantsWithSortOrderUseCase(
    private val plantRepository: PlantRepository,
    private val getRemainWateringDateUseCase: GetRemainWateringDateUseCase
) {
    operator fun invoke(sortOrder: PlantListSortOrder): Flow<List<Plant>> {
        return plantRepository.getPlants().map { list ->
            list.applySort(sortOrder)
        }
    }

    private fun List<Plant>.applySort(sortOrder: PlantListSortOrder): List<Plant> {
        return when (sortOrder) {
            PlantListSortOrder.CREATED_LATEST -> {
                sortedByDescending { plant -> plant.createdDate }
            }

            PlantListSortOrder.CREATED_EARLIEST -> {
                sortedBy { plant -> plant.createdDate }
            }

            PlantListSortOrder.WATERING -> {
                sortedBy { plant -> getRemainWateringDateUseCase(plant) }
            }
        }
    }
}