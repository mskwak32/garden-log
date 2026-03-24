package com.mskwak.domain.usecase.watering

import com.mskwak.domain.repository.WateringLogRepository
import java.time.LocalDate

class GetWateringDatesForExportUseCase(
    private val wateringLogRepository: WateringLogRepository
) {
    suspend operator fun invoke(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): Set<LocalDate> {
        return wateringLogRepository.getWateringDatesByRange(plantId, startDate, endDate).toSet()
    }
}
