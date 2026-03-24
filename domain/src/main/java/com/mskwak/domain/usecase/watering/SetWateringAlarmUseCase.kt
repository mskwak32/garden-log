package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.WateringAlarmRepository
import java.time.LocalDateTime

class SetWateringAlarmUseCase(
    private val plantRepository: PlantRepository,
    private val wateringAlarmRepository: WateringAlarmRepository
) {
    suspend operator fun invoke(plantId: Int, isActive: Boolean) {
        if (isActive) {
            // 다음 알람이 없는 경우는 등록하지 않음
            getNextAlarmDateTime(plantId)?.let { nextDateTime ->
                wateringAlarmRepository.setWateringAlarm(plantId, nextDateTime)
            }
        } else {
            wateringAlarmRepository.cancelWateringAlarm(plantId)
        }
    }

    private suspend fun getNextAlarmDateTime(plantId: Int): LocalDateTime? {
        val plant = plantRepository.getPlant(plantId)?.also {
            if (it.waterPeriod == 0) return null
        } ?: return null
        val nextDate = plant.lastWateringDate.plusDays(plant.waterPeriod.toLong())
        var nextDateTime = LocalDateTime.of(nextDate, plant.wateringAlarm.time)

        // nextDateTime이 과거인지 체크. 과거이면 미래가 될 때까지 waterPeriod 더함
        val currentTime = LocalDateTime.now()
        while (nextDateTime < currentTime) {
            nextDateTime = nextDateTime.plusDays(plant.waterPeriod.toLong())
        }

        return nextDateTime
    }
}