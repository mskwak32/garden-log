package com.mskwak.domain.usecase.watering

import com.mskwak.domain.model.Plant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 물주기 기간이 남은 경우 양수 예) 2일 후 = 2
 * 물주기 기간이 지난 경우 음수 예) 2일 전 = -2
 * 물주기 설정이 없는 경우 Int.MAX_VALUE
 */
class GetRemainWateringDateUseCase {

    operator fun invoke(plant: Plant): Int {
        if (plant.waterPeriod == 0) {
            return Int.MAX_VALUE
        }

        val today = LocalDate.now()
        val nextDate = plant.lastWateringDate.plusDays(plant.waterPeriod.toLong())
        return ChronoUnit.DAYS.between(today, nextDate).toInt()
    }
}