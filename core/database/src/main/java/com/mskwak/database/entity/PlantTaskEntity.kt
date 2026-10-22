package com.mskwak.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "plant_task",
    indices = [Index(value = ["plantId"])]
)
data class PlantTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val title: String,
    val content: String?,
    // ONE_TIME / REPEAT (enum 매핑은 data 모듈에서 처리)
    val type: String,
    val date: LocalDate,
    val periodDays: Int?,
    @Embedded(prefix = "alarm_") val alarm: AlarmEntity,
    val createdAt: LocalDateTime
)
