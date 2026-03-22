package com.mskwak.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "watering_log",
    indices = [Index(value = ["plantId", "date"], unique = true)]
)
data class WateringLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val date: LocalDate
)
