package com.mskwak.domain.usecase.task

import com.mskwak.domain.repository.PlantTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class GetNearestTaskDueDatesUseCase(
    private val plantTaskRepository: PlantTaskRepository,
    private val getNextDueDateUseCase: GetNextDueDateUseCase
) {
    /**
     * plantId별 가장 이른 예정일(지난 일정 포함). 할일이 없는 식물은 키가 없다
     */
    operator fun invoke(): Flow<Map<Int, LocalDate>> =
        plantTaskRepository.observeAllTasks().map { tasks ->
            tasks.groupBy { it.plantId }
                .mapValues { (_, list) -> list.minOf { getNextDueDateUseCase(it) } }
        }
}
