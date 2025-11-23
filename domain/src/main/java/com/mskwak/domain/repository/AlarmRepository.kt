package com.mskwak.domain.repository

import java.time.LocalDateTime

interface AlarmRepository {
    fun setWateringAlarm(plantId: Int, nextAlarmDateTime: LocalDateTime)
    fun cancelWateringAlarm(plantId: Int)
}