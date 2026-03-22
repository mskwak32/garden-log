package com.mskwak.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mskwak.database.dao.DiaryDao
import com.mskwak.database.dao.PictureDao
import com.mskwak.database.dao.PlantDao
import com.mskwak.database.dao.WateringLogDao
import com.mskwak.database.entity.*

@Database(
    entities = [PlantEntity::class, DiaryEntity::class, PictureEntity::class, DiaryPictureCrossRef::class, WateringLogEntity::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(GardenDatabaseConverter::class)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun diaryDao(): DiaryDao
    abstract fun pictureDao(): PictureDao
    abstract fun wateringLogDao(): WateringLogDao

    companion object {
        const val DATABASE_NAME = "garden.db"
    }
}
