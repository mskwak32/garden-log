package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import kotlinx.coroutines.CancellationException

class UpdatePlantTaskUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    suspend operator fun invoke(task: PlantTask, isDateChanged: Boolean): WateringUpdateResult {
        // 날짜를 바꾸지 않았으면 기존 기준일을 유지
        val date = if (isDateChanged) task.date
        else plantTaskRepository.getTask(task.id)?.date ?: task.date
        plantTaskRepository.updateTask(task.copy(date = date))
        return try {
            setPlantTaskAlarmUseCase(task.id, task.alarm.isActive)
            WateringUpdateResult.CHANGED
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            WateringUpdateResult.ALARM_FAILED
        }
    }
}
