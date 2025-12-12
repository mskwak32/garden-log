package com.mskwak.domain.useCase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow

class GetPlantUseCase(
    private val plantRepository: PlantRepository
) {
    operator fun invoke(plantId: Int): Flow<Plant?> = plantRepository.getPlantFlow(plantId)
}