package com.mskwak.domain.usecase.task

import com.mskwak.domain.repository.PlantTaskRepository
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

class UpdatePlantTaskLogUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val setPlantTaskAlarmUseCase: SetPlantTaskAlarmUseCase
) {
    suspend operator fun invoke(taskId: Int, date: LocalDate, isDone: Boolean): WateringUpdateResult {
        if (!plantTaskRepository.setTaskLog(taskId, date, isDone)) {
            return WateringUpdateResult.UNCHANGED
        }
        return try {
            // 1회성 완료로 할일이 사라졌으면 알람 취소, 남아 있으면 활성 알람만 재예약
            val task = plantTaskRepository.getTask(taskId)
            setPlantTaskAlarmUseCase(taskId, task?.alarm?.isActive == true)
            WateringUpdateResult.CHANGED
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            WateringUpdateResult.ALARM_FAILED
        }
    }
}
