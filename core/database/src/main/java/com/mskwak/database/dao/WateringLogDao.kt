package com.mskwak.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskwak.database.entity.WateringLogEntity
import java.time.LocalDate

@Dao
interface WateringLogDao {
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
}
