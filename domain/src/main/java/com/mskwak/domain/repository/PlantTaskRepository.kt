package com.mskwak.domain.repository

import com.mskwak.domain.model.PlantTask
import com.mskwak.domain.model.PlantTaskLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PlantTaskRepository {
    /**
     * 생성순으로 정렬하고 latestDoneDate, isDoneToday를 채워서 반환
     */
    fun observeTasks(plantId: Int): Flow<List<PlantTask>>
    fun observeAllTasks(): Flow<List<PlantTask>>
    suspend fun getTask(taskId: Int): PlantTask?

    /**
     * @return taskId
     */
    suspend fun addTask(task: PlantTask): Int

    /**
     * type, plantId, createdAt은 기존 값을 유지한다
     */
    suspend fun updateTask(task: PlantTask)
    suspend fun deleteTask(taskId: Int)

    /**
     * 목표 상태를 적용한다. 1회성 완료 시 이력 저장과 할일 삭제를 함께 처리한다.
     * @return 변경이 있으면 true
     * @throws com.mskwak.domain.model.WateringValidationException 수확 식물 또는 범위 밖 날짜 추가
     */
    suspend fun setTaskLog(taskId: Int, date: LocalDate, isDone: Boolean): Boolean
    fun observeTaskLogDates(taskId: Int): Flow<List<LocalDate>>
    fun observePlantTaskLogs(plantId: Int): Flow<List<PlantTaskLog>>
    suspend fun getPlantTaskLogsByRange(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<PlantTaskLog>

    suspend fun getActiveAlarmTaskIds(): List<Int>
    suspend fun getTaskIdsByPlantId(plantId: Int): List<Int>
    suspend fun updateAlarmActivation(taskId: Int, isActive: Boolean)

    /**
     * 식물 삭제용. 할일과 완료 이력을 모두 삭제한다
     */
    suspend fun deleteByPlantId(plantId: Int)
}
