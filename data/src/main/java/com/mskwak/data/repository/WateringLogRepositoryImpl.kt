package com.mskwak.data.repository

import androidx.room.withTransaction
import com.mskwak.database.GardenDatabase
import com.mskwak.database.dao.WateringLogDao
import com.mskwak.database.entity.WateringLogEntity
import com.mskwak.domain.model.WateringValidationException
import com.mskwak.domain.model.WateringValidationReason
import com.mskwak.domain.repository.WateringLogRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class WateringLogRepositoryImpl @Inject constructor(
    private val wateringLogDao: WateringLogDao,
    private val database: GardenDatabase
) : WateringLogRepository {
    override fun observeWateringDates(plantId: Int): Flow<List<LocalDate>> =
        wateringLogDao.observeWateringDates(plantId)

    override suspend fun getWateringDates(plantId: Int): List<LocalDate> {
        return wateringLogDao.getWateringDates(plantId)
    }

    override suspend fun setWateringLog(plantId: Int, date: LocalDate, isWatered: Boolean): Boolean {
        return database.withTransaction {
            val plant = requireNotNull(database.plantDao().getPlant(plantId))
            if (plant.harvestDate != null) {
                throw WateringValidationException(WateringValidationReason.HARVESTED)
            }
            // 범위 밖 기존 기록은 삭제할 수 있지만 신규 추가는 금지
            if (isWatered && (date < plant.createdDate || date > LocalDate.now())) {
                throw WateringValidationException(WateringValidationReason.OUTSIDE_RANGE)
            }
            val exists = wateringLogDao.hasWateringLog(plantId, date)
            // 토글 대신 목표 상태를 적용해 같은 요청이 반복되어도 기록을 뒤집지 않음
            if (exists == isWatered) return@withTransaction false
            if (isWatered) {
                wateringLogDao.insertWateringLog(WateringLogEntity(plantId = plantId, date = date))
            } else {
                wateringLogDao.deleteWateringLog(plantId, date)
            }
            // 이력 변경과 계산 기준 갱신을 같은 트랜잭션에서 처리
            val latest = wateringLogDao.getLatestWateringDate(plantId) ?: plant.createdDate
            database.plantDao().updateLastWateringDate(plantId, latest)
            true
        }
    }

    override suspend fun addWateringLog(plantId: Int, date: LocalDate) {
        setWateringLog(plantId, date, true)
    }

    override suspend fun deleteWateringLog(plantId: Int, date: LocalDate) {
        setWateringLog(plantId, date, false)
    }

    override suspend fun hasWateringLog(plantId: Int, date: LocalDate): Boolean {
        return wateringLogDao.hasWateringLog(plantId, date)
    }

    override suspend fun getWateringDatesByRange(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocalDate> {
        return wateringLogDao.getWateringDatesByRange(plantId, startDate, endDate)
    }

    override suspend fun getLatestWateringDateBefore(plantId: Int, date: LocalDate): LocalDate? {
        return wateringLogDao.getLatestWateringDateBefore(plantId, date)
    }
}
