package com.mskwak.data.repository

import androidx.room.Room
import com.mskwak.database.GardenDatabase
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.domain.model.WateringValidationException
import com.mskwak.domain.model.WateringValidationReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class PlantTaskPersistenceTest {
    private lateinit var database: GardenDatabase
    private lateinit var plants: PlantRepositoryImpl
    private lateinit var tasks: PlantTaskRepositoryImpl
    private val today = LocalDate.now()
    private val planted = today.minusDays(10)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(), GardenDatabase::class.java
        ).allowMainThreadQueries().build()
        plants = PlantRepositoryImpl(
            database.plantDao(),
            database.pictureDao(),
            database.wateringLogDao(),
            database
        )
        tasks = PlantTaskRepositoryImpl(database.plantTaskDao(), database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun completingOneTimeTaskDeletesTaskAndKeepsLogWithTitleSnapshot() = runBlocking {
        val plantId = plants.addPlant(plant())
        val taskId = tasks.addTask(task(plantId, PlantTaskType.ONE_TIME, "분갈이"))
        assertTrue(tasks.setTaskLog(taskId, today, true))
        assertNull(tasks.getTask(taskId))
        val logs = tasks.observePlantTaskLogs(plantId).first()
        assertEquals(listOf("분갈이"), logs.map { it.title })
        assertEquals(taskId, logs.single().taskId)
        // 삭제된 할일에 대한 요청은 변경 없음
        assertFalse(tasks.setTaskLog(taskId, today, false))
    }

    @Test
    fun repeatTaskLogCanBeAddedForPastDateAndCancelledWithoutDuplicates() = runBlocking {
        val plantId = plants.addPlant(plant())
        val taskId = tasks.addTask(task(plantId, PlantTaskType.REPEAT, "영양제"))
        val past = today.minusDays(3)
        assertTrue(tasks.setTaskLog(taskId, past, true))
        assertFalse(tasks.setTaskLog(taskId, past, true))
        assertNotNull(tasks.getTask(taskId))
        assertEquals(listOf(past), tasks.observeTaskLogDates(taskId).first())
        assertTrue(tasks.setTaskLog(taskId, past, false))
        assertFalse(tasks.setTaskLog(taskId, past, false))
        assertTrue(tasks.observeTaskLogDates(taskId).first().isEmpty())
    }

    @Test
    fun deletingRepeatTaskKeepsLogs() = runBlocking {
        val plantId = plants.addPlant(plant())
        val taskId = tasks.addTask(task(plantId, PlantTaskType.REPEAT, "영양제"))
        tasks.setTaskLog(taskId, today, true)
        tasks.deleteTask(taskId)
        assertNull(tasks.getTask(taskId))
        assertEquals(listOf("영양제"), tasks.observePlantTaskLogs(plantId).first().map { it.title })
    }

    @Test
    fun deleteByPlantIdRemovesTasksAndLogsOfThatPlantOnly() = runBlocking {
        val plantId = plants.addPlant(plant())
        val otherId = plants.addPlant(plant().copy(name = "other"))
        val taskId = tasks.addTask(task(plantId, PlantTaskType.REPEAT, "영양제"))
        val otherTaskId = tasks.addTask(task(otherId, PlantTaskType.REPEAT, "다른 할일"))
        tasks.setTaskLog(taskId, today, true)
        tasks.setTaskLog(otherTaskId, today, true)
        tasks.deleteByPlantId(plantId)
        assertTrue(tasks.getTaskIdsByPlantId(plantId).isEmpty())
        assertTrue(tasks.observePlantTaskLogs(plantId).first().isEmpty())
        assertEquals(listOf(otherTaskId), tasks.getTaskIdsByPlantId(otherId))
        assertEquals(1, tasks.observePlantTaskLogs(otherId).first().size)
    }

    @Test
    fun harvestedPlantAndOutOfRangeDatesAreRejected() = runBlocking {
        val plantId = plants.addPlant(plant())
        val taskId = tasks.addTask(task(plantId, PlantTaskType.REPEAT, "영양제"))
        for (date in listOf(planted.minusDays(1), today.plusDays(1))) {
            expectRejected(WateringValidationReason.OUTSIDE_RANGE) { tasks.setTaskLog(taskId, date, true) }
        }
        tasks.setTaskLog(taskId, today, true)
        plants.updateHarvestStatus(plantId, today, null)
        expectRejected(WateringValidationReason.HARVESTED) { tasks.setTaskLog(taskId, today, false) }
        expectRejected(WateringValidationReason.HARVESTED) { tasks.setTaskLog(taskId, today.minusDays(1), true) }
        assertEquals(listOf(today), tasks.observeTaskLogDates(taskId).first())
    }

    @Test
    fun observeTasksReflectsLatestDoneDateAndIsDoneToday() = runBlocking {
        val plantId = plants.addPlant(plant())
        val first = tasks.addTask(task(plantId, PlantTaskType.REPEAT, "첫째"))
        val second = tasks.addTask(task(plantId, PlantTaskType.REPEAT, "둘째"))
        val past = today.minusDays(2)
        tasks.setTaskLog(first, past, true)
        var list = tasks.observeTasks(plantId).first()
        assertEquals(listOf(first, second), list.map { it.id })
        assertEquals(past, list[0].latestDoneDate)
        assertFalse(list[0].isDoneToday)
        assertNull(list[1].latestDoneDate)
        tasks.setTaskLog(first, today, true)
        list = tasks.observeTasks(plantId).first()
        assertEquals(today, list[0].latestDoneDate)
        assertTrue(list[0].isDoneToday)
        tasks.setTaskLog(first, today, false)
        list = tasks.observeTasks(plantId).first()
        assertEquals(past, list[0].latestDoneDate)
        assertFalse(list[0].isDoneToday)
    }

    private fun plant() = Plant(
        id = 0, name = "식물", createdDate = planted, waterPeriod = 3,
        lastWateringDate = planted, wateringAlarm = Alarm(LocalTime.NOON, false),
        picture = null, memo = null
    )

    private fun task(plantId: Int, type: PlantTaskType, title: String) = PlantTask(
        plantId = plantId, title = title, content = null, type = type, date = today,
        periodDays = if (type == PlantTaskType.REPEAT) 7 else null,
        alarm = Alarm(LocalTime.NOON, false), createdAt = LocalDateTime.now()
    )

    private suspend fun expectRejected(reason: WateringValidationReason, block: suspend () -> Unit) {
        try {
            block()
            fail("변경을 거부해야 함")
        } catch (exception: WateringValidationException) {
            assertEquals(reason, exception.reason)
        }
    }
}
