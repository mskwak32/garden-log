package com.mskwak.data.repository

import com.mskwak.data.mapper.toPlant
import com.mskwak.data.mapper.toPlantEntity
import com.mskwak.database.dao.PlantDao
import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

internal class PlantRepositoryImpl @Inject constructor(
    private val plantDao: PlantDao
) : PlantRepository {
    override suspend fun addPlant(plant: Plant): Int {
        val id = plantDao.insertPlant(plant.toPlantEntity()).toInt()
        Timber.d("add new plant id= $id")
        return id
    }

    override suspend fun updatePlant(plant: Plant) {
        plantDao.updatePlant(plant.toPlantEntity())
        Timber.d("update plant id= ${plant.id}")
    }

    override suspend fun deletePlant(plant: Plant) {
        plantDao.deletePlant(plant.toPlantEntity())
        Timber.d("delete plant id= ${plant.id}")
    }

    override fun getPlant(plantId: Int): Flow<Plant> {
        return plantDao.getPlant(plantId).map { it.toPlant() }
    }

    override fun getPlants(): Flow<List<Plant>> {
        return plantDao.getPlants().map { list ->
            list.map { it.toPlant() }
        }
    }

    override suspend fun getPlantName(plantId: Int): String {
        return plantDao.getPlantName(plantId)
    }

    override suspend fun getPlantNames(): Map<Int, String> {
        return plantDao.getPlantNames()
    }

    override suspend fun getPlantIdsWithAlarmActivation(): Map<Int, Boolean> {
        return plantDao.getPlantIdsWithAlarmActivation()
    }

    override suspend fun updateWateringAlarmActivation(isActive: Boolean, plantId: Int) {
        plantDao.updateWateringAlarmActivation(isActive, plantId)
    }
}