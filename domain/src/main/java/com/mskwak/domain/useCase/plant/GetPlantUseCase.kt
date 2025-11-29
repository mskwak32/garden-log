package com.mskwak.domain.useCase.plant

import com.mskwak.domain.repository.PlantRepository

class GetPlantUseCase(
    private val plantRepository: PlantRepository
) {
    operator fun invoke(plantId: Int) = plantRepository.getPlant(plantId)
}