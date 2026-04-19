package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.WateringLogRepository
import java.time.LocalDate

class CancelTodayWateringUseCase(
    private val plantRepository: PlantRepository,
    private val wateringLogRepository: WateringLogRepository,
    private val setWateringAlarmUseCase: SetWateringAlarmUseCase
) {
    suspend operator fun invoke(plantId: Int) {
        val today = LocalDate.now()
        val plant = plantRepository.getPlant(plantId) ?: return

        wateringLogRepository.deleteWateringLog(plantId, today)

        val prevDate = wateringLogRepository.getLatestWateringDateBefore(plantId, today)
            ?: plant.createdDate
        plantRepository.updatePlant(plant.copy(lastWateringDate = prevDate))

        // FLAG_UPDATE_CURRENT로 기존 알람을 자동 교체; 복원된 날짜가 과거면 while 루프가 미래로 조정
        if (plant.wateringAlarm.isActive) {
            setWateringAlarmUseCase(plant.id, true)
        }
    }
}
