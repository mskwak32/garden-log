package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.WateringLogRepository
import java.time.LocalDate

class AddWateringLogUseCase(
    private val wateringLogRepository: WateringLogRepository
) {
    suspend operator fun invoke(plantId: Int, date: LocalDate) {
        wateringLogRepository.addWateringLog(plantId, date)
    }
}
