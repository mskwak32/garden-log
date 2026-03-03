package com.mskwak.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mskwak.database.dao.DiaryDao
import com.mskwak.database.dao.PictureDao
import com.mskwak.database.dao.PlantDao
import com.mskwak.database.entity.DiaryEntity
import com.mskwak.database.entity.DiaryPictureCrossRef
import com.mskwak.database.entity.PictureEntity
import com.mskwak.database.entity.PlantEntity

@Database(
    entities = [PlantEntity::class, DiaryEntity::class, PictureEntity::class, DiaryPictureCrossRef::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(GardenDatabaseConverter::class)
abstract class GardenDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun diaryDao(): DiaryDao
    abstract fun pictureDao(): PictureDao

    companion object {
        const val DATABASE_NAME = "garden.db"
    }
}
