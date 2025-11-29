package com.mskwak.domain.repository

import java.time.LocalDateTime

interface WateringAlarmRepository {
    fun setWateringAlarm(plantId: Int, nextAlarmDateTime: LocalDateTime)
    fun cancelWateringAlarm(plantId: Int)
}