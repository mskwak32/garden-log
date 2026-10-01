package com.mskwak.domain.usecase.watering

import java.time.LocalDate

class WateringNowUseCase(private val updateWateringLogUseCase: UpdateWateringLogUseCase) {
    suspend operator fun invoke(plantId: Int): WateringUpdateResult =
        updateWateringLogUseCase(plantId, LocalDate.now(), true)
}
