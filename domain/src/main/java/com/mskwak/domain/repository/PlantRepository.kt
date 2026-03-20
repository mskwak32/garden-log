package com.mskwak.domain.repository

import com.mskwak.domain.model.Plant
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PlantRepository {
    /**
     * @return plantId
     */
    suspend fun addPlant(plant: Plant): Int
    suspend fun updatePlant(plant: Plant)
    suspend fun deletePlant(plant: Plant)
    fun getPlantFlow(plantId: Int): Flow<Plant?>
    suspend fun getPlant(plantId: Int): Plant?
    fun getPlants(): Flow<List<Plant>>
    suspend fun getPlantName(plantId: Int): String?

    /**
     * @return Map<plantId, plantName>
     */
    suspend fun getPlantNames(): Map<Int, String>
    suspend fun getPlantIdsWithAlarmActivation(): Map<Int, Boolean>
    suspend fun updateWateringAlarmActivation(isActive: Boolean, plantId: Int)
    suspend fun updateHarvestStatus(plantId: Int, harvestDate: LocalDate?, harvestMemo: String?)
}
