package com.mskwak.domain.repository

import java.time.LocalDate

interface WateringLogRepository {
    suspend fun addWateringLog(plantId: Int, date: LocalDate)
    suspend fun hasWateringLog(plantId: Int, date: LocalDate): Boolean

    suspend fun getWateringDatesByRange(
        plantId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LocalDate>

    suspend fun deleteWateringLog(plantId: Int, date: LocalDate)
    suspend fun getLatestWateringDateBefore(plantId: Int, date: LocalDate): LocalDate?
}
