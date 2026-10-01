package com.mskwak.data.repository

import androidx.room.withTransaction
import com.mskwak.data.mapper.toPictureEntity
import com.mskwak.data.mapper.toPlant
import com.mskwak.data.mapper.toPlantEntity
import com.mskwak.database.GardenDatabase
import com.mskwak.database.dao.PictureDao
import com.mskwak.database.dao.PlantDao
import com.mskwak.database.dao.WateringLogDao
import com.mskwak.database.entity.PlantWithWatering
import com.mskwak.database.entity.WateringLogEntity
import com.mskwak.domain.model.Plant
import com.mskwak.domain.model.WateringValidationException
import com.mskwak.domain.model.WateringValidationReason
import com.mskwak.domain.repository.PlantRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

internal class PlantRepositoryImpl @Inject constructor(
    private val plantDao: PlantDao,
    private val pictureDao: PictureDao,
    private val wateringLogDao: WateringLogDao,
    private val database: GardenDatabase
) : PlantRepository {

    override suspend fun addPlant(plant: Plant): Int {
        if (plant.createdDate > LocalDate.now()) {
            throw WateringValidationException(WateringValidationReason.FUTURE_CREATED_DATE)
        }
        if (plant.lastWateringDate < plant.createdDate || plant.lastWateringDate > LocalDate.now()) {
            throw WateringValidationException(WateringValidationReason.OUTSIDE_RANGE)
        }
        return database.withTransaction {
            val pictureId =
                plant.picture?.let { pictureDao.insertPicture(it.toPictureEntity()).toInt() }
            val id = plantDao.insertPlant(plant.toPlantEntity(pictureId)).toInt()
            Timber.d("add new plant id=$id")
            // 식물 등록과 최초 이력 저장을 함께 완료하거나 함께 취소
            wateringLogDao.insertWateringLog(
                WateringLogEntity(plantId = id, date = plant.lastWateringDate)
            )
            id
        }
    }

    override suspend fun updatePlant(plant: Plant) {
        database.withTransaction {
            val existing = requireNotNull(plantDao.getPlant(plant.id))
            val dates = wateringLogDao.getWateringDates(plant.id)
            // 기존 미래 날짜를 그대로 둔 다른 항목 편집은 허용하고 날짜 변경만 검증
            if (existing.createdDate != plant.createdDate) {
                if (plant.createdDate > LocalDate.now()) {
                    throw WateringValidationException(WateringValidationReason.FUTURE_CREATED_DATE)
                }
                // 이력을 삭제해서 날짜를 맞추지 않고, 기존 기록보다 늦은 심은 날짜를 차단
                if (dates.any { it < plant.createdDate }) {
                    throw WateringValidationException(WateringValidationReason.CREATED_AFTER_WATERING_DATE)
                }
            }
            val existingPicture = existing.pictureId?.let { pictureDao.getPicture(it) }
            val newPicture = plant.picture

            val pictureId: Int?
            when {
                newPicture == null -> {
                    existing.pictureId?.let { pictureDao.deletePicture(it) }
                    pictureId = null
                }

                existingPicture?.path == newPicture.path -> {
                    // 이미지가 동일하면 기존 pictureId 재사용 (삭제 후 재삽입 시 일시적 null 방지)
                    pictureId = existing.pictureId
                }

                else -> {
                    existing.pictureId?.let { pictureDao.deletePicture(it) }
                    pictureId = pictureDao.insertPicture(newPicture.toPictureEntity()).toInt()
                }
            }

            // 편집 화면의 오래된 날짜와 수확 상태로 최신 저장값을 덮어쓰지 않음
            val updated = plant.copy(
                lastWateringDate = dates.maxOrNull() ?: plant.createdDate,
                harvestDate = existing.harvestDate,
                harvestMemo = existing.harvestMemo
            )
            plantDao.updatePlant(updated.toPlantEntity(pictureId))
            Timber.d("update plant id=${plant.id}")
        }
    }

    override suspend fun deletePlant(plant: Plant) {
        val existing = plantDao.getPlant(plant.id)
        existing?.pictureId?.let { pictureDao.deletePicture(it) }
        plantDao.deletePlant(plant.toPlantEntity(existing?.pictureId))
        Timber.d("delete plant id=${plant.id}")
    }

    override fun getPlantFlow(plantId: Int): Flow<Plant?> =
        plantDao.observePlantWithWatering(plantId).map { record ->
            record?.toPlant(LocalDate.now())
        }

    override suspend fun getPlant(plantId: Int): Plant? =
        plantDao.getPlantWithWatering(plantId)?.toPlant(LocalDate.now())

    // 관계 조회 하나로 사진·이력 변경을 함께 반영하고 목록의 날짜 기준을 통일
    override fun getPlants(): Flow<List<Plant>> =
        plantDao.observePlantsWithWatering().map { records ->
            val today = LocalDate.now()
            records.map { it.toPlant(today) }
        }

    private fun PlantWithWatering.toPlant(today: LocalDate): Plant {
        val latest = wateringDates.maxOrNull()
        // 이력이 없으면 계산 기준만 심은 날짜로 대체하고, 실제 마지막 기록은 null 유지
        return plant.toPlant(picture).copy(
            lastWateringDate = latest ?: plant.createdDate,
            latestWateringDate = latest,
            isWateredToday = today in wateringDates
        )
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

    override suspend fun updateHarvestStatus(
        plantId: Int,
        harvestDate: LocalDate?,
        harvestMemo: String?
    ) {
        plantDao.updateHarvestStatus(harvestDate, harvestMemo, plantId)
    }
}
