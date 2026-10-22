package com.mskwak.domain.usecase.task

import com.mskwak.domain.repository.PlantTaskRepository

class UpdatePlantTaskAlarmActivationUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    suspend operator fun invoke(taskId: Int, isActive: Boolean) {
        plantTaskRepository.updateAlarmActivation(taskId, isActive)
        setPlantTaskAlarmUseCase(taskId, isActive)
    }
}
