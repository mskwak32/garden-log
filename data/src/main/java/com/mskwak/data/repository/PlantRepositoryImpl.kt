package com.mskwak.data.repository

import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlantRepositoryImpl @Inject constructor(

) : PlantRepository {
    override suspend fun addPlant(plant: Plant): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlant(plant: Plant) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePlant(plant: Plant) {
        TODO("Not yet implemented")
    }

    override fun getPlant(plantId: Int): Flow<Plant> {
        TODO("Not yet implemented")
    }

    override fun getPlants(): Flow<List<Plant>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlantName(plantId: Int): String {
        TODO("Not yet implemented")
    }

    override suspend fun getPlantNames(): Map<Int, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlantIdWithAlarmActivation(): Map<Int, Boolean> {
        TODO("Not yet implemented")
    }
}