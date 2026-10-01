package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.WateringAlarmRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SetWateringAlarmUseCase(
    private val plantRepository: PlantRepository,
    private val wateringAlarmRepository: WateringAlarmRepository
) {
    suspend operator fun invoke(plantId: Int, isActive: Boolean) {
        if (isActive) {
            // 다음 알람이 없는 경우는 등록하지 않음
            val nextDateTime = getNextAlarmDateTime(plantId)
            if (nextDateTime != null) {
                wateringAlarmRepository.setWateringAlarm(plantId, nextDateTime)
            } else {
                wateringAlarmRepository.cancelWateringAlarm(plantId)
            }
        } else {
            wateringAlarmRepository.cancelWateringAlarm(plantId)
        }
    }

    private suspend fun getNextAlarmDateTime(plantId: Int): LocalDateTime? {
        val plant = plantRepository.getPlant(plantId)?.also {
            if (it.waterPeriod <= 0 || it.isHarvested) return null
        } ?: return null
        val nextDate = plant.lastWateringDate.plusDays(plant.waterPeriod.toLong())
        var nextDateTime = LocalDateTime.of(nextDate, plant.wateringAlarm.time)

        // 지난 알람 주기를 한 번에 건너뛰어, 오래된 기록에서도 반복 계산을 피함
        val currentTime = LocalDateTime.now()
        if (nextDateTime < currentTime) {
            val days = ChronoUnit.DAYS.between(nextDateTime.toLocalDate(), currentTime.toLocalDate())
            // 경과 일수를 물주기 간격으로 나눠 현재 날짜 근처의 주기로 이동
            val periods = days / plant.waterPeriod
            nextDateTime = nextDateTime.plusDays(periods * plant.waterPeriod)
            // 나머지 일수나 오늘의 알람 시각 때문에 아직 과거이면 다음 주기로 이동
            if (nextDateTime < currentTime) nextDateTime = nextDateTime.plusDays(plant.waterPeriod.toLong())
        }

        return nextDateTime
    }
}
