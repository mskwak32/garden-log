package com.mskwak.domain.usecase.plant

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase

class AddPlantUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant) {
        val id = plantRepository.addPlant(plant)
        setWateringAlarmUseCase(id, plant.wateringAlarm.isActive)
    }
}