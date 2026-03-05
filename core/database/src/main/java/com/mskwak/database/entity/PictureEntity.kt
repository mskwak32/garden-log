package com.mskwak.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picture")
data class PictureEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String,
    val fileName: String,
    val createdAt: Long
)
