package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GetTaskRemainDaysUseCase(
    private val getNextDueDateUseCase: GetNextDueDateUseCase
) {
    /**
     * 양수는 남은 일수, 0은 오늘, 음수는 지난 일수
     */
    operator fun invoke(task: PlantTask, today: LocalDate = LocalDate.now()): Int {
        return ChronoUnit.DAYS.between(today, getNextDueDateUseCase(task)).toInt()
    }
}
