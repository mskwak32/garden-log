package com.mskwak.domain.useCase.watering

import com.mskwak.domain.repository.PlantRepository

class UpdateWateringAlarmActivationUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plantId: Int, isActive: Boolean) {
        plantRepository.updateWateringAlarmActivation(isActive, plantId)
        setWateringAlarmUseCase(plantId, isActive)
    }
}