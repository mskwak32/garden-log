package com.mskwak.domain.repository

import java.time.LocalDateTime

interface PlantTaskAlarmRepository {
    fun setTaskAlarm(taskId: Int, nextAlarmDateTime: LocalDateTime)
    fun cancelTaskAlarm(taskId: Int)
}
