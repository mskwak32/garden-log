package com.mskwak.domain.repository

import java.time.LocalDate

interface WateringLogRepository {
    suspend fun addWateringLog(plantId: Int, date: LocalDate)
    suspend fun hasWateringLog(plantId: Int, date: LocalDate): Boolean
}
