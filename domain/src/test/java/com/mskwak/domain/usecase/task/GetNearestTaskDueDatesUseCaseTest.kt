package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.domain.repository.PlantTaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.LocalDate

class GetNearestTaskDueDatesUseCaseTest {
    @Test
    fun 식물별_가장_이른_예정일을_반환한다() = runBlocking {
        val d = LocalDate.of(2026, 10, 10)
        val tasks = listOf(
            task(PlantTaskType.ONE_TIME, d.plusDays(5), plantId = 1),
            task(PlantTaskType.ONE_TIME, d.minusDays(2), plantId = 1),
            task(PlantTaskType.ONE_TIME, d, plantId = 2)
        )
        val repo = fakeRepository(tasks)
        val result = GetNearestTaskDueDatesUseCase(repo, GetNextDueDateUseCase())().first()

        assertEquals(mapOf(1 to d.minusDays(2), 2 to d), result)
    }
}

// observeAllTasks만 사용하므로 나머지 메서드는 구현하지 않음
internal fun fakeRepository(tasks: List<PlantTask>): PlantTaskRepository =
    Proxy.newProxyInstance(
        PlantTaskRepository::class.java.classLoader,
        arrayOf(PlantTaskRepository::class.java)
    ) { _, method, _ ->
        if (method.name == "observeAllTasks") flowOf(tasks) else error("unused: ${method.name}")
    } as PlantTaskRepository
