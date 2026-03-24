package com.mskwak.domain.usecase.plant

import com.mskwak.domain.repository.PlantRepository

class GetPlantNameUseCase(
    private val plantRepository: PlantRepository
) {
    suspend fun getName(plantId: Int): String? {
        return plantRepository.getPlantName(plantId)
    }

    /**
     * @return Map<plantId, plantName>
     */
    suspend fun getNames(): Map<Int, String> {
        return plantRepository.getPlantNames()
    }
}