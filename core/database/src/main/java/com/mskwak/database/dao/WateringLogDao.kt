package com.mskwak.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskwak.database.entity.WateringLogEntity
import java.time.LocalDate

@Dao
interface WateringLogDao {
    @Query("SELECT date FROM watering_log WHERE plantId = :plantId ORDER BY date")
    fun observeWateringDates(plantId: Int): kotlinx.coroutines.flow.Flow<List<LocalDate>>

    @Query("SELECT date FROM watering_log WHERE plantId = :plantId ORDER BY date")
    suspend fun getWateringDates(plantId: Int): List<LocalDate>

    @Query("SELECT MAX(date) FROM watering_log WHERE plantId = :plantId")
    suspend fun getLatestWateringDate(plantId: Int): LocalDate?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWateringLog(entity: WateringLogEntity)

    @Query("SELECT COUNT(*) > 0 FROM watering_log WHERE plantId = :plantId AND date = :date")
    suspend fun hasWateringLog(plantId: Int, date: LocalDate): Boolean

    @Query("SELECT date FROM watering_log WHERE plantId = :plantId AND date BETWEEN :startDate AND :endDate")
    suspend fun getWateringDatesByRange(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocalDate>

    @Query("DELETE FROM watering_log WHERE plantId = :plantId AND date = :date")
    suspend fun deleteWateringLog(plantId: Int, date: LocalDate)

    @Query("SELECT MAX(date) FROM watering_log WHERE plantId = :plantId AND date < :date")
    suspend fun getLatestWateringDateBefore(plantId: Int, date: LocalDate): LocalDate?
}
