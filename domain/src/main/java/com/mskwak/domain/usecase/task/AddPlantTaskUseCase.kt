package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import kotlinx.coroutines.CancellationException

class AddPlantTaskUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    suspend operator fun invoke(task: PlantTask): WateringUpdateResult {
        val id = plantTaskRepository.addTask(task)
        // 저장 완료 후 알람 실패로 할일 저장을 반복하지 않음
        return try {
            if (task.alarm.isActive) setPlantTaskAlarmUseCase(id, true)
            WateringUpdateResult.CHANGED
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            WateringUpdateResult.ALARM_FAILED
        }
    }
}
