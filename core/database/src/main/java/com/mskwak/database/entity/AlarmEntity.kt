package com.mskwak.database.entity

import java.time.LocalTime

data class AlarmEntity(
    val time: LocalTime,
    val isActive: Boolean
)
