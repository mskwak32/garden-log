package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTaskLog
import com.mskwak.domain.repository.PlantTaskRepository
import java.time.LocalDate

class GetPlantTaskLogsByRangeUseCase(
    private val plantTaskRepository: PlantTaskRepository
) {
    suspend operator fun invoke(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PlantTaskLog> =
        plantTaskRepository.getPlantTaskLogsByRange(plantId, startDate, endDate)
}
