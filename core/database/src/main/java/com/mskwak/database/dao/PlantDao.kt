package com.mskwak.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import com.mskwak.database.entity.PlantEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlantDao {
    @Insert
    suspend fun insertPlant(plantEntity: PlantEntity): Long

    @Update
    suspend fun updatePlant(plantEntity: PlantEntity)

    @Delete
    suspend fun deletePlant(plantEntity: PlantEntity)

    @Query("SELECT * FROM plant")
    fun getPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plant WHERE id = :id")
    fun getPlant(id: Int): Flow<PlantEntity>

    @Query("UPDATE plant SET lastWateringDate = :date WHERE id = :plantId")
    suspend fun updateLastWatering(date: LocalDate, plantId: Int)

    @Query("UPDATE plant SET watering_alarm_isActive = :isActive WHERE id = :plantId")
    suspend fun updateWateringAlarmActivation(isActive: Boolean, plantId: Int)

    @Query("SELECT name FROM plant WHERE id = :plantId")
    suspend fun getPlantName(plantId: String): String

    @Query("SELECT id AS plantId, name AS plantName FROM plant")
    suspend fun getPlantNames(): Map<
            @MapColumn(columnName = "plantId") Int,
            @MapColumn(columnName = "plantName") String>

    @Query("SELECT id AS plantId, watering_alarm_isActive AS alarmActivation FROM plant")
    suspend fun getPlantIdWithAlarmActivation(): Map<
            @MapColumn(columnName = "plantId") Int,
            @MapColumn(columnName = "alarmActivation") Boolean>
}