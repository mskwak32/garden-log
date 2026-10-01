package com.mskwak.domain.usecase.watering

import java.time.LocalDate

class CancelTodayWateringUseCase(private val updateWateringLogUseCase: UpdateWateringLogUseCase) {
    suspend operator fun invoke(plantId: Int): WateringUpdateResult =
        updateWateringLogUseCase(plantId, LocalDate.now(), false)
}
