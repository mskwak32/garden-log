package com.mskwak.domain.usecase.task

import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.domain.repository.PlantRepository
import com.mskwak.domain.repository.PlantTaskAlarmRepository
import com.mskwak.domain.repository.PlantTaskRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SetPlantTaskAlarmUseCaseTest {
    private val now = LocalDateTime.of(2026, 10, 22, 10, 0)
    private val calls = mutableListOf<String>()

    private val alarmRepository = object : PlantTaskAlarmRepository {
        override fun setTaskAlarm(taskId: Int, nextAlarmDateTime: LocalDateTime) {
            calls += "set $nextAlarmDateTime"
        }

        override fun cancelTaskAlarm(taskId: Int) {
            calls += "cancel"
        }
    }

    private val plant = Plant(
        id = 1, name = "p", createdDate = LocalDate.of(2026, 1, 1), waterPeriod = 3,
        lastWateringDate = LocalDate.of(2026, 1, 1), wateringAlarm = Alarm(LocalTime.NOON, false),
        picture = null, memo = null
    )

    private fun useCase(task: PlantTask, plant: Plant? = null) = SetPlantTaskAlarmUseCase(
        stub<PlantTaskRepository>("getTask" to task),
        stub<PlantRepository>("getPlant" to plant),
        alarmRepository,
        GetNextDueDateUseCase()
    )

    @Test
    fun 반복성_지난_예정일은_주기_단위로_건너뛴다() {
        // 기준일 10/1, 7일 주기 -> 10/22 09:00은 지났으므로 10/29 09:00
        val t = task(date = LocalDate.of(2026, 10, 1))
        assertEquals(LocalDateTime.of(2026, 10, 29, 9, 0), useCase(t).calculateNextAlarmDateTime(t, now))
    }

    @Test
    fun 반복성_오늘_알람_시각이_아직이면_오늘로_예약한다() {
        val t = task(date = LocalDate.of(2026, 10, 1), time = LocalTime.of(18, 0))
        assertEquals(LocalDateTime.of(2026, 10, 22, 18, 0), useCase(t).calculateNextAlarmDateTime(t, now))
    }

    @Test
    fun 일회성_지난_시각은_등록하지_않는다() {
        val t = task(PlantTaskType.ONE_TIME, LocalDate.of(2026, 10, 22), null)
        assertNull(useCase(t).calculateNextAlarmDateTime(t, now))
    }

    @Test
    fun 일회성_지난_시각이면_invoke에서_알람을_취소한다() = runBlocking {
        val t = task(PlantTaskType.ONE_TIME, LocalDate.of(2020, 1, 1), null)
        useCase(t, plant = plant).invoke(t.id, true)
        assertEquals(listOf("cancel"), calls)
    }
}

private inline fun <reified T : Any> stub(vararg answers: Pair<String, Any?>): T {
    val map = answers.toMap()
    return Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, m, _ ->
        if (m.name in map) map[m.name] else error("unused: ${m.name}")
    } as T
}
