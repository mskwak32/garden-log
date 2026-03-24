package com.mskwak.data.repository

import com.mskwak.database.dao.WateringLogDao
import com.mskwak.database.entity.WateringLogEntity
import com.mskwak.domain.repository.WateringLogRepository
import java.time.LocalDate
import javax.inject.Inject

internal class WateringLogRepositoryImpl @Inject constructor(
    private val wateringLogDao: WateringLogDao
) : WateringLogRepository {

    override suspend fun addWateringLog(plantId: Int, date: LocalDate) {
        wateringLogDao.insertWateringLog(WateringLogEntity(plantId = plantId, date = date))
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
}
