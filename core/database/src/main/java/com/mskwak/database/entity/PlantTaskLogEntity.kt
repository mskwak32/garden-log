package com.mskwak.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

// 할일 삭제 후에도 이력을 유지하기 위해 FK를 두지 않음
@Entity(
    tableName = "plant_task_log",
    indices = [
        Index(value = ["plantId", "date"]),
        Index(value = ["taskId", "date"], unique = true)
    ]
)
data class PlantTaskLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val taskId: Int?,
    val title: String,
    val date: LocalDate
)
