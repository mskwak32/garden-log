package com.mskwak.domain.useCase.watering

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import java.time.LocalDate

class WateringNowUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plant: Plant) {
        val now = LocalDate.now()
        plantRepository.updatePlant(
            plant.copy(lastWateringDate = now)
        )

        // 알람이 설정되어있으면 다음 물주기 시간으로 알람 재세팅
        if (plant.wateringAlarm.isActive) {
            setWateringAlarmUseCase(plant.id, true)
        }
    }
}