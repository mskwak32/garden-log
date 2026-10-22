package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import java.time.LocalDate

class GetNextDueDateUseCase {
    operator fun invoke(task: PlantTask): LocalDate {
        if (task.type == PlantTaskType.ONE_TIME) return task.date
        val latestDone = task.latestDoneDate
        // 이력이 없거나 기준일 이전이면 기준일이 첫 예정일
        if (latestDone == null || latestDone < task.date) return task.date
        return latestDone.plusDays((task.periodDays ?: 1).toLong())
    }
}
