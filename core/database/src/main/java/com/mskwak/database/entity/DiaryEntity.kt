package com.mskwak.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "diary",
    indices = [Index(value = ["plantId"])]
)
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int,
    val memo: String,
    val createdDate: LocalDate
)
