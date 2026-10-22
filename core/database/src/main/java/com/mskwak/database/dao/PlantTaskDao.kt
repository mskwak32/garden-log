package com.mskwak.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mskwak.database.entity.PlantTaskEntity
import com.mskwak.database.entity.PlantTaskLogEntity
import com.mskwak.database.entity.PlantTaskWithDone
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlantTaskDao {
    // 서브쿼리가 plant_task_log를 읽으므로 이력 변경에도 Flow가 다시 방출됨
    @Query(
        """
        SELECT t.*,
            (SELECT MAX(l.date) FROM plant_task_log l WHERE l.taskId = t.id) AS latestDoneDate,
            EXISTS(SELECT 1 FROM plant_task_log l WHERE l.taskId = t.id AND l.date = :today) AS isDoneToday
        FROM plant_task t WHERE t.plantId = :plantId ORDER BY t.createdAt, t.id
        """
    )
    fun observeTasks(plantId: Int, today: LocalDate): Flow<List<PlantTaskWithDone>>

    @Query(
        """
        SELECT t.*,
            (SELECT MAX(l.date) FROM plant_task_log l WHERE l.taskId = t.id) AS latestDoneDate,
            EXISTS(SELECT 1 FROM plant_task_log l WHERE l.taskId = t.id AND l.date = :today) AS isDoneToday
        FROM plant_task t ORDER BY t.createdAt, t.id
        """
    )
    fun observeAllTasks(today: LocalDate): Flow<List<PlantTaskWithDone>>

    @Query("SELECT * FROM plant_task WHERE id = :taskId")
    suspend fun getTask(taskId: Int): PlantTaskEntity?

    @Query(
        """
        SELECT t.*,
            (SELECT MAX(l.date) FROM plant_task_log l WHERE l.taskId = t.id) AS latestDoneDate,
            EXISTS(SELECT 1 FROM plant_task_log l WHERE l.taskId = t.id AND l.date = :today) AS isDoneToday
        FROM plant_task t WHERE t.id = :taskId
        """
    )
    suspend fun getTaskWithDone(taskId: Int, today: LocalDate): PlantTaskWithDone?

    @Insert
    suspend fun insertTask(entity: PlantTaskEntity): Long

    @Update
    suspend fun updateTask(entity: PlantTaskEntity)

    @Query("DELETE FROM plant_task WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("DELETE FROM plant_task WHERE plantId = :plantId")
    suspend fun deleteTasksByPlantId(plantId: Int)

    @Query("SELECT id FROM plant_task WHERE plantId = :plantId")
    suspend fun getTaskIdsByPlantId(plantId: Int): List<Int>

    @Query("SELECT id FROM plant_task WHERE alarm_isActive = 1")
    suspend fun getActiveAlarmTaskIds(): List<Int>

    @Query("UPDATE plant_task SET alarm_isActive = :isActive WHERE id = :taskId")
    suspend fun updateAlarmActivation(taskId: Int, isActive: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskLog(entity: PlantTaskLogEntity)

    @Query("DELETE FROM plant_task_log WHERE taskId = :taskId AND date = :date")
    suspend fun deleteTaskLog(taskId: Int, date: LocalDate)

    @Query("SELECT COUNT(*) > 0 FROM plant_task_log WHERE taskId = :taskId AND date = :date")
    suspend fun hasTaskLog(taskId: Int, date: LocalDate): Boolean

    @Query("SELECT date FROM plant_task_log WHERE taskId = :taskId ORDER BY date")
    fun observeTaskLogDates(taskId: Int): Flow<List<LocalDate>>

    @Query("SELECT * FROM plant_task_log WHERE plantId = :plantId ORDER BY date, id")
    fun observePlantTaskLogs(plantId: Int): Flow<List<PlantTaskLogEntity>>

    @Query("SELECT * FROM plant_task_log WHERE plantId = :plantId AND date BETWEEN :startDate AND :endDate ORDER BY date, id")
    suspend fun getPlantTaskLogsByRange(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PlantTaskLogEntity>

    @Query("DELETE FROM plant_task_log WHERE plantId = :plantId")
    suspend fun deleteTaskLogsByPlantId(plantId: Int)
}
