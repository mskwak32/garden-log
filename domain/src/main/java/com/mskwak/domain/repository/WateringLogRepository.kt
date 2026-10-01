package com.mskwak.domain.repository

import java.time.LocalDate

interface WateringLogRepository {
    fun observeWateringDates(plantId: Int): kotlinx.coroutines.flow.Flow<List<LocalDate>>
    suspend fun getWateringDates(plantId: Int): List<LocalDate>
    suspend fun setWateringLog(plantId: Int, date: LocalDate, isWatered: Boolean): Boolean
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
