package com.mskwak.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mskwak.database.dao.DiaryDao
import com.mskwak.database.dao.PlantDao
import com.mskwak.database.entity.DiaryEntity
import com.mskwak.database.entity.PlantEntity

@Database(
    entities = [PlantEntity::class, DiaryEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(DatabaseConverter::class)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun diaryDao(): DiaryDao

    companion object {
        const val DATABASE_NAME = "garden.db"
    }
}