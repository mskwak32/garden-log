package com.mskwak.domain.useCase.watering

import com.mskwak.domain.repository.PlantRepository
import java.time.LocalDate

class WateringNowUseCase(
    private val plantRepository: PlantRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase,
    private val addWateringLogUseCase: AddWateringLogUseCase
) {
    suspend operator fun invoke(plantId: Int) {
        val now = LocalDate.now()
        val plant = plantRepository.getPlant(plantId) ?: return

        plantRepository.updatePlant(
            plant.copy(lastWateringDate = now)
        )

        // 물주기 날짜 이력 기록 (같은 날 중복 시 무시)
        addWateringLogUseCase(plantId, now)

        // 알람이 설정되어있으면 다음 물주기 시간으로 알람 재세팅
        if (plant.wateringAlarm.isActive) {
            setWateringAlarmUseCase(plant.id, true)
        }
    }
}