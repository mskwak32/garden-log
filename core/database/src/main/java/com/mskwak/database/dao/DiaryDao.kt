package com.mskwak.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mskwak.database.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DiaryDao {
    @Insert
    suspend fun insertDiary(diaryEntity: DiaryEntity)

    @Update
    suspend fun updateDiary(diaryEntity: DiaryEntity)

    @Delete
    suspend fun deleteDiary(diaryEntity: DiaryEntity)

    @Query("DELETE FROM diary WHERE plantId = :plantId")
    suspend fun deleteDiariesByPlantId(plantId: Int)

    @Query("SELECT * FROM diary WHERE plantId = :plantId ORDER BY createdDate DESC LIMIT :limit")
    fun getDiariesByPlantId(plantId: Int, limit: Int): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary WHERE plantId = :plantId AND createdDate BETWEEN :startDate AND :endDate ORDER BY createdDate DESC")
    fun getDiariesByPlantId(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary WHERE id = :id")
    fun getDiary(id: Int): Flow<DiaryEntity>

    @Query("SELECT * FROM diary WHERE createdDate BETWEEN :startDate AND :endDate ORDER BY createdDate DESC")
    fun getDiaries(startDate: LocalDate, endDate: LocalDate): Flow<List<DiaryEntity>>
}