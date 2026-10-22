package com.mskwak.domain.usecase.task

import com.mskwak.domain.repository.PlantTaskRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GetTaskLogDatesUseCase(
    private val plantTaskRepository: PlantTaskRepository
) {
    operator fun invoke(taskId: Int): Flow<List<LocalDate>> =
        plantTaskRepository.observeTaskLogDates(taskId)
}
