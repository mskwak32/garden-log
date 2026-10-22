package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.repository.PlantTaskRepository
import kotlinx.coroutines.flow.Flow

class GetPlantTasksUseCase(
    private val plantTaskRepository: PlantTaskRepository
) {
    operator fun invoke(plantId: Int): Flow<List<PlantTask>> =
        plantTaskRepository.observeTasks(plantId)
}
