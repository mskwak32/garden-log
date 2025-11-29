package com.mskwak.domain.useCase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.useCase.watering.SetWateringAlarmUseCase

class UpdatePlantUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant) {
        plantRepository.updatePlant(plant)
        setWateringAlarmUseCase(plant.id, plant.wateringAlarm.isActive)
    }
}