package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTaskLog
import com.mskwak.domain.repository.PlantTaskRepository
import kotlinx.coroutines.flow.Flow

class GetPlantTaskLogsUseCase(
    private val plantTaskRepository: PlantTaskRepository
) {
    operator fun invoke(plantId: Int): Flow<List<PlantTaskLog>> =
        plantTaskRepository.observePlantTaskLogs(plantId)
}
