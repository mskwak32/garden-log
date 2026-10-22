package com.mskwak.data.repository

import androidx.room.withTransaction
import com.mskwak.data.mapper.toPlantTask
import com.mskwak.data.mapper.toPlantTaskEntity
import com.mskwak.data.mapper.toPlantTaskLog
import com.mskwak.database.GardenDatabase
import com.mskwak.database.dao.PlantTaskDao
import com.mskwak.database.entity.PlantTaskLogEntity
import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskLog
import com.mskwak.domain.model.PlantTaskType
import com.mskwak.domain.model.WateringValidationException
import com.mskwak.domain.model.WateringValidationReason
import com.mskwak.domain.repository.PlantTaskRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlantTaskRepositoryImpl @Inject constructor(
    private val plantTaskDao: PlantTaskDao,
    private val database: GardenDatabase
) : PlantTaskRepository {
    override fun observeTasks(plantId: Int): Flow<List<PlantTask>> =
        plantTaskDao.observeTasks(plantId, LocalDate.now()).map { list -> list.map { it.toPlantTask() } }

    override fun observeAllTasks(): Flow<List<PlantTask>> =
        plantTaskDao.observeAllTasks(LocalDate.now()).map { list -> list.map { it.toPlantTask() } }

    // 알람의 다음 예정일 계산에 최신 완료일이 필요하므로 이력까지 함께 조회
    override suspend fun getTask(taskId: Int): PlantTask? =
        plantTaskDao.getTaskWithDone(taskId, LocalDate.now())?.toPlantTask()

    override suspend fun addTask(task: PlantTask): Int =
        plantTaskDao.insertTask(task.toPlantTaskEntity()).toInt()

    override suspend fun updateTask(task: PlantTask) {
        database.withTransaction {
            val origin = plantTaskDao.getTask(task.id) ?: return@withTransaction
            plantTaskDao.updateTask(
                task.toPlantTaskEntity().copy(
                    type = origin.type,
                    plantId = origin.plantId,
                    createdAt = origin.createdAt
                )
            )
        }
    }

    override suspend fun deleteTask(taskId: Int) {
        plantTaskDao.deleteTask(taskId)
    }

    override suspend fun setTaskLog(taskId: Int, date: LocalDate, isDone: Boolean): Boolean {
        return database.withTransaction {
            // 이미 완료된 1회성 등 삭제된 할일은 변경하지 않음
            val task = plantTaskDao.getTask(taskId) ?: return@withTransaction false
            val plant = requireNotNull(database.plantDao().getPlant(task.plantId))
            if (plant.harvestDate != null) {
                throw WateringValidationException(WateringValidationReason.HARVESTED)
            }
            if (isDone && (date < plant.createdDate || date > LocalDate.now())) {
                throw WateringValidationException(WateringValidationReason.OUTSIDE_RANGE)
            }
            val exists = plantTaskDao.hasTaskLog(taskId, date)
            if (exists == isDone) return@withTransaction false
            if (isDone) {
                // 할일이 삭제되어도 표시할 수 있도록 현재 제목을 함께 저장
                plantTaskDao.insertTaskLog(
                    PlantTaskLogEntity(plantId = task.plantId, taskId = taskId, title = task.title, date = date)
                )
                if (task.type == PlantTaskType.ONE_TIME.name) {
                    plantTaskDao.deleteTask(taskId)
                }
            } else {
                plantTaskDao.deleteTaskLog(taskId, date)
            }
            true
        }
    }

    override fun observeTaskLogDates(taskId: Int): Flow<List<LocalDate>> =
        plantTaskDao.observeTaskLogDates(taskId)

    override fun observePlantTaskLogs(plantId: Int): Flow<List<PlantTaskLog>> =
        plantTaskDao.observePlantTaskLogs(plantId).map { list -> list.map { it.toPlantTaskLog() } }

    override suspend fun getPlantTaskLogsByRange(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PlantTaskLog> =
        plantTaskDao.getPlantTaskLogsByRange(plantId, startDate, endDate).map { it.toPlantTaskLog() }

    override suspend fun getActiveAlarmTaskIds(): List<Int> = plantTaskDao.getActiveAlarmTaskIds()

    override suspend fun getTaskIdsByPlantId(plantId: Int): List<Int> =
        plantTaskDao.getTaskIdsByPlantId(plantId)

    override suspend fun updateAlarmActivation(taskId: Int, isActive: Boolean) {
        plantTaskDao.updateAlarmActivation(taskId, isActive)
    }

    override suspend fun deleteByPlantId(plantId: Int) {
        database.withTransaction {
            plantTaskDao.deleteTasksByPlantId(plantId)
            plantTaskDao.deleteTaskLogsByPlantId(plantId)
        }
    }
}
