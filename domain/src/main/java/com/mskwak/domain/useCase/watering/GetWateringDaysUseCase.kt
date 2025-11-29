package com.mskwak.domain.useCase.watering

import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.WateringDays
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * 물주기 주기설정이 없는 경우 마지막 물준날짜로부터 D+00
 * 물주기 주기설정이 있는 경우 다음 물주기까지 D-00
 * 물주기가 지난 경우 D+00
 */
class GetWateringDaysUseCase(
    private val getRemainWateringDateUseCase: GetRemainWateringDateUseCase
) {
    operator fun invoke(plant: Plant): WateringDays {
        return if (plant.waterPeriod == 0) {
            WateringDays(getDaysFromLastWatering(plant), true)
        } else {
            val days = abs(getRemainWateringDateUseCase(plant))
            WateringDays(days, days < 0)
        }
    }

    private fun getDaysFromLastWatering(plant: Plant): Int {
        val today = LocalDate.now()

        if (!today.isAfter(plant.lastWateringDate)) {
            return 0
        }

        return ChronoUnit.DAYS.between(plant.lastWateringDate, today).toInt()
    }
}