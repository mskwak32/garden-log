package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.PlantTaskAlarmRepository
import com.mskwak.domain.repository.PlantTaskRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SetPlantTaskAlarmUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val plantRepository: PlantRepository,
    private val plantTaskAlarmRepository: PlantTaskAlarmRepository,
    private val getNextDueDateUseCase: GetNextDueDateUseCase
) {
    suspend operator fun invoke(taskId: Int, isActive: Boolean) {
        val nextDateTime = if (isActive) getNextAlarmDateTime(taskId, LocalDateTime.now()) else null
        if (nextDateTime != null) {
            plantTaskAlarmRepository.setTaskAlarm(taskId, nextDateTime)
        } else {
            plantTaskAlarmRepository.cancelTaskAlarm(taskId)
        }
    }

    // 등록할 알람이 없으면 null (할일 없음, 수확된 식물, 지난 1회성)
    private suspend fun getNextAlarmDateTime(taskId: Int, now: LocalDateTime): LocalDateTime? {
        val task = plantTaskRepository.getTask(taskId) ?: return null
        if (plantRepository.getPlant(task.plantId)?.isHarvested != false) return null
        return calculateNextAlarmDateTime(task, now)
    }

    internal fun calculateNextAlarmDateTime(task: PlantTask, now: LocalDateTime): LocalDateTime? {
        var dateTime = LocalDateTime.of(getNextDueDateUseCase(task), task.alarm.time)
        if (dateTime >= now) return dateTime
        // 지난 1회성은 다시 울리지 않음
        if (task.type == PlantTaskType.ONE_TIME) return null

        val period = task.periodDays?.toLong()?.takeIf { it > 0 } ?: return null
        // 지난 주기를 한 번에 건너뛰어 오래된 기준일에서도 반복 계산을 피함
        val days = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())
        dateTime = dateTime.plusDays(days / period * period)
        // 나머지 일수나 오늘의 알람 시각 때문에 아직 과거이면 다음 주기로 이동
        if (dateTime < now) dateTime = dateTime.plusDays(period)
        return dateTime
    }
}
