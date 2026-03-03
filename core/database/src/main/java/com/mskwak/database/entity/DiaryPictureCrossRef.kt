package com.mskwak.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "diary_picture",
    primaryKeys = ["diaryId", "pictureId"],
    indices = [Index("diaryId")]
)
data class DiaryPictureCrossRef(
    val diaryId: Int,
    val pictureId: Int
)
