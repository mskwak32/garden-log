package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.PlantTaskType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetNextDueDateUseCaseTest {
    private val useCase = GetNextDueDateUseCase()
    private val base = LocalDate.of(2026, 10, 1)

    @Test
    fun 일회성은_예정일을_반환한다() {
        val task = task(PlantTaskType.ONE_TIME, base, periodDays = null, latestDoneDate = base.plusDays(3))
        assertEquals(base, useCase(task))
    }

    @Test
    fun 반복성_이력이_없으면_기준일을_반환한다() {
        assertEquals(base, useCase(task(date = base)))
    }

    @Test
    fun 반복성_이력이_기준일_이상이면_이력에_주기를_더한다() {
        assertEquals(base.plusDays(10), useCase(task(date = base, latestDoneDate = base.plusDays(3))))
        assertEquals(base.plusDays(7), useCase(task(date = base, latestDoneDate = base)))
    }

    @Test
    fun 반복성_이력이_기준일_이전이면_기준일을_반환한다() {
        assertEquals(base, useCase(task(date = base, latestDoneDate = base.minusDays(2))))
    }
}
