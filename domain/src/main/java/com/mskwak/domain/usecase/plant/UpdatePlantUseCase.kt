package com.mskwak.domain.usecase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase

class UpdatePlantUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant) {
        plantRepository.updatePlant(plant)
        setWateringAlarmUseCase(plant.id, plant.wateringAlarm.isActive)
    }
}