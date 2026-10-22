package com.mskwak.domain.usecase.task

import com.mskwak.domain.repository.PlantTaskRepository

class DeletePlantTaskUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    suspend operator fun invoke(taskId: Int) {
        setPlantTaskAlarmUseCase(taskId, false)
        plantTaskRepository.deleteTask(taskId)
    }
}
