package com.mskwak.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "plant"
)
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdDate: LocalDate,
    val waterPeriod: Int,
    val lastWateringDate: LocalDate,
    @Embedded(prefix = "watering_alarm_") val wateringAlarm: AlarmEntity,
    val pictureId: Int?,
    val memo: String?
)