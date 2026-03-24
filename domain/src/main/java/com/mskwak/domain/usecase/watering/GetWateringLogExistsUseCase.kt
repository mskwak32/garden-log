package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.WateringLogRepository
import java.time.LocalDate

class GetWateringLogExistsUseCase(
    private val wateringLogRepository: WateringLogRepository
) {
    suspend operator fun invoke(plantId: Int, date: LocalDate): Boolean {
        return wateringLogRepository.hasWateringLog(plantId, date)
    }
}
