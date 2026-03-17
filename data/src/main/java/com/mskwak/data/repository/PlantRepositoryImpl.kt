package com.mskwak.data.repository

import com.mskwak.data.mapper.toPlant
import com.mskwak.data.mapper.toPlantEntity
import com.mskwak.data.mapper.toPictureEntity
import com.mskwak.database.dao.PictureDao
import com.mskwak.database.dao.PlantDao
import com.mskwak.domain.model.Plant
import com.mskwak.domain.repository.PlantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class PlantRepositoryImpl @Inject constructor(
    private val plantDao: PlantDao,
    private val pictureDao: PictureDao
) : PlantRepository {

    override suspend fun addPlant(plant: Plant): Int {
        val pictureId = plant.picture?.let { pictureDao.insertPicture(it.toPictureEntity()).toInt() }
        val id = plantDao.insertPlant(plant.toPlantEntity(pictureId)).toInt()
        Timber.d("add new plant id=$id")
        return id
    }

    override suspend fun updatePlant(plant: Plant) {
        val existing = plantDao.getPlant(plant.id)
        val existingPicture = existing?.pictureId?.let { pictureDao.getPicture(it) }
        val newPicture = plant.picture

        val pictureId: Int?
        when {
            newPicture == null -> {
                existing?.pictureId?.let { pictureDao.deletePicture(it) }
                pictureId = null
            }
            existingPicture?.path == newPicture.path -> {
                // 이미지가 동일하면 기존 pictureId 재사용 (삭제 후 재삽입 시 일시적 null 방지)
                pictureId = existing.pictureId
            }
            else -> {
                existing?.pictureId?.let { pictureDao.deletePicture(it) }
                pictureId = pictureDao.insertPicture(newPicture.toPictureEntity()).toInt()
            }
        }

        plantDao.updatePlant(plant.toPlantEntity(pictureId))
        Timber.d("update plant id=${plant.id}")
    }

    override suspend fun deletePlant(plant: Plant) {
        val existing = plantDao.getPlant(plant.id)
        existing?.pictureId?.let { pictureDao.deletePicture(it) }
        plantDao.deletePlant(plant.toPlantEntity(existing?.pictureId))
        Timber.d("delete plant id=${plant.id}")
    }

    override fun getPlantFlow(plantId: Int): Flow<Plant?> {
        return plantDao.getPlantFlow(plantId).flatMapLatest { plantEntity ->
            if (plantEntity == null) return@flatMapLatest flowOf(null)
            val pictureId = plantEntity.pictureId
            if (pictureId == null) {
                flowOf(plantEntity.toPlant(null))
            } else {
                pictureDao.getPictureFlow(pictureId).map { picture ->
                    plantEntity.toPlant(picture)
                }
            }
        }
    }

    override suspend fun getPlant(plantId: Int): Plant? {
        val plantEntity = plantDao.getPlant(plantId) ?: return null
        val picture = plantEntity.pictureId?.let { pictureDao.getPicture(it) }
        return plantEntity.toPlant(picture)
    }

    override fun getPlants(): Flow<List<Plant>> {
        return plantDao.getPlants().flatMapLatest { plants ->
            if (plants.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(plants.map { plantEntity ->
                val pictureId = plantEntity.pictureId
                if (pictureId == null) {
                    flowOf(plantEntity.toPlant(null))
                } else {
                    pictureDao.getPictureFlow(pictureId).map { picture ->
                        plantEntity.toPlant(picture)
                    }
                }
            }) { it.toList() }
        }
    }

    override suspend fun getPlantName(plantId: Int): String? {
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
