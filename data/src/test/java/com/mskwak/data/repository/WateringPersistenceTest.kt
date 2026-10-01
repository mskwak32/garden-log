package com.mskwak.data.repository

import androidx.room.Room
import com.mskwak.database.GardenDatabase
import com.mskwak.database.entity.WateringLogEntity
import com.mskwak.domain.model.Alarm
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.WateringValidationException
import com.mskwak.domain.model.WateringValidationReason
import com.mskwak.domain.repository.WateringAlarmRepository
import com.mskwak.domain.usecase.watering.SetWateringAlarmUseCase
import com.mskwak.domain.usecase.watering.UpdateWateringLogUseCase
import com.mskwak.domain.usecase.watering.WateringUpdateResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
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
class WateringPersistenceTest {
    private lateinit var database: GardenDatabase
    private lateinit var plants: PlantRepositoryImpl
    private lateinit var logs: WateringLogRepositoryImpl
    private val today = LocalDate.now()
    private val planted = today.minusDays(10)
    private val initial = today.minusDays(5)

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
        logs = WateringLogRepositoryImpl(database.wateringLogDao(), database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun plantListObservesWateringChangesWithoutChangingOtherPlants() = runBlocking {
        val firstId = plants.addPlant(plant())
        val secondId = plants.addPlant(plant().copy(name = "second"))
        val updates = Channel<List<Plant>>(Channel.UNLIMITED)
        val observer = launch { plants.getPlants().collect { updates.send(it) } }
        try {
            val initialList = withTimeout(5_000) { updates.receive() }
            assertEquals(setOf(firstId, secondId), initialList.map { it.id }.toSet())
            logs.setWateringLog(firstId, today, true)
            val changed = withTimeout(5_000) {
                var list = updates.receive()
                while (list.first { it.id == firstId }.latestWateringDate != today) {
                    list = updates.receive()
                }
                list
            }
            assertTrue(changed.first { it.id == firstId }.isWateredToday)
            assertEquals(initial, changed.first { it.id == secondId }.latestWateringDate)
            assertFalse(changed.first { it.id == secondId }.isWateredToday)
        } finally {
            observer.cancel()
            updates.close()
        }
    }

    @Test
    fun registrationAndHistoryChangesKeepLatestDateConsistent() = runBlocking {
        val id = plants.addPlant(plant())
        assertEquals(listOf(initial), logs.getWateringDates(id))
        val older = initial.minusDays(1)
        assertTrue(logs.setWateringLog(id, older, true))
        assertFalse(logs.setWateringLog(id, older, true))
        assertEquals(listOf(older, initial), logs.getWateringDates(id))
        assertEquals(initial, plants.getPlant(id)!!.lastWateringDate)
        assertTrue(logs.setWateringLog(id, today, true))
        assertEquals(today, plants.getPlant(id)!!.lastWateringDate)
        assertTrue(plants.getPlant(id)!!.isWateredToday)
        assertTrue(logs.setWateringLog(id, older, false))
        assertEquals(today, plants.getPlant(id)!!.lastWateringDate)
        assertTrue(logs.setWateringLog(id, today, false))
        assertEquals(initial, plants.getPlant(id)!!.lastWateringDate)
        assertFalse(plants.getPlant(id)!!.isWateredToday)
        assertTrue(logs.setWateringLog(id, initial, false))
        assertNull(plants.getPlant(id)!!.latestWateringDate)
        assertEquals(planted, plants.getPlant(id)!!.lastWateringDate)
        assertFalse(logs.setWateringLog(id, initial, false))
    }

    @Test
    fun additionsEnforceBoundsButLegacyOutOfRangeRecordsCanBeDeleted() = runBlocking {
        expectRejected(WateringValidationReason.FUTURE_CREATED_DATE) {
            plants.addPlant(plant().copy(createdDate = today.plusDays(1)))
        }
        for (date in listOf(planted.minusDays(1), today.plusDays(1))) {
            expectRejected(WateringValidationReason.OUTSIDE_RANGE) {
                plants.addPlant(plant().copy(lastWateringDate = date))
            }
        }
        assertTrue(database.plantDao().getAllPlants().isEmpty())
        val id = plants.addPlant(plant())
        for (date in listOf(planted.minusDays(1), today.plusDays(1))) {
            expectRejected(WateringValidationReason.OUTSIDE_RANGE) { logs.setWateringLog(id, date, true) }
            database.wateringLogDao().insertWateringLog(WateringLogEntity(plantId = id, date = date))
            assertTrue(logs.setWateringLog(id, date, false))
        }
        assertTrue(logs.setWateringLog(id, planted, true))
        assertTrue(logs.setWateringLog(id, today, true))
    }

    @Test
    fun harvestedPlantRejectsBothAdditionAndCancellation() = runBlocking {
        val id = plants.addPlant(plant())
        plants.updateHarvestStatus(id, today, null)
        expectRejected(WateringValidationReason.HARVESTED) { logs.setWateringLog(id, today, true) }
        expectRejected(WateringValidationReason.HARVESTED) { logs.setWateringLog(id, initial, false) }
        assertEquals(listOf(initial), logs.getWateringDates(id))
    }

    @Test
    fun plantEditChecksOldestRecordAndPreservesCurrentWateringDate() = runBlocking {
        val id = plants.addPlant(plant())
        val stale = plants.getPlant(id)!!
        logs.setWateringLog(id, today, true)
        expectRejected(WateringValidationReason.CREATED_AFTER_WATERING_DATE) {
            plants.updatePlant(stale.copy(createdDate = initial.plusDays(1)))
        }
        plants.updatePlant(stale.copy(createdDate = initial, name = "수정"))
        assertEquals(initial, plants.getPlant(id)!!.createdDate)
        assertEquals(today, plants.getPlant(id)!!.lastWateringDate)
        assertEquals("수정", plants.getPlant(id)!!.name)
        logs.setWateringLog(id, initial, false)
        logs.setWateringLog(id, today, false)
        plants.updatePlant(stale.copy(createdDate = today))
        assertEquals(today, plants.getPlant(id)!!.lastWateringDate)
        assertNull(plants.getPlant(id)!!.latestWateringDate)
        expectRejected(WateringValidationReason.FUTURE_CREATED_DATE) {
            plants.updatePlant(stale.copy(createdDate = today.plusDays(1)))
        }
        plants.updateHarvestStatus(id, today, "수확")
        plants.updatePlant(stale.copy(createdDate = today, name = "수확 후 수정"))
        assertEquals(today, plants.getPlant(id)!!.harvestDate)
        assertEquals("수확", plants.getPlant(id)!!.harvestMemo)
    }

    @Test
    fun alarmFailureReportsSavedRecordInsteadOfSaveFailure() = runBlocking {
        val id = plants.addPlant(plant().copy(wateringAlarm = Alarm(LocalTime.NOON, true)))
        var scheduleAttempts = 0
        val alarmRepository = object : WateringAlarmRepository {
            override fun setWateringAlarm(plantId: Int, nextAlarmDateTime: LocalDateTime) {
                scheduleAttempts++
                throw IllegalStateException("알람 예약 실패")
            }

            override fun cancelWateringAlarm(plantId: Int) = Unit
        }
        val update = UpdateWateringLogUseCase(logs, plants, SetWateringAlarmUseCase(plants, alarmRepository))
        assertEquals(WateringUpdateResult.ALARM_FAILED, update(id, today, true))
        assertEquals(listOf(initial, today), logs.getWateringDates(id))
        assertEquals(today, plants.getPlant(id)!!.lastWateringDate)
        assertEquals(WateringUpdateResult.UNCHANGED, update(id, today, true))
        assertEquals(1, scheduleAttempts)
    }

    @Test
    fun failedInitialLogInsertRollsBackPlantRegistration() = runBlocking {
        // 실제 SQLite 오류를 주입해 식물만 남는 부분 저장이 없는지 검증
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_initial_log BEFORE INSERT ON watering_log BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        expectFailure { plants.addPlant(plant()) }
        assertTrue(database.plantDao().getAllPlants().isEmpty())
    }

    @Test
    fun failedDateUpdateRollsBackWateringLogChange() = runBlocking {
        val id = plants.addPlant(plant())
        // 이력 변경 뒤 날짜 갱신을 실패시켜 추가·삭제 모두 롤백되는지 검증
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_date_update BEFORE UPDATE OF lastWateringDate ON plant BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        expectFailure { logs.setWateringLog(id, today, true) }
        assertEquals(listOf(initial), logs.getWateringDates(id))
        assertEquals(initial, plants.getPlant(id)!!.lastWateringDate)
        expectFailure { logs.setWateringLog(id, initial, false) }
        assertEquals(listOf(initial), logs.getWateringDates(id))
    }

    private fun plant() = Plant(
        id = 0, name = "식물", createdDate = planted, waterPeriod = 3,
        lastWateringDate = initial, wateringAlarm = Alarm(LocalTime.NOON, false),
        picture = null, memo = null
    )

    private suspend fun expectRejected(reason: WateringValidationReason, block: suspend () -> Unit) {
        try {
            block()
            fail("변경을 거부해야 함")
        } catch (exception: WateringValidationException) {
            assertEquals(reason, exception.reason)
        }
    }

    private suspend fun expectFailure(block: suspend () -> Unit) {
        var failure: Exception? = null
        try {
            block()
        } catch (exception: Exception) {
            failure = exception
        }
        assertNotNull("DB 실패가 호출자에게 전달되어야 함", failure)
    }
}
