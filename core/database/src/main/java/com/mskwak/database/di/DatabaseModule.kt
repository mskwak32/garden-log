package com.mskwak.database.di

import android.app.Application
import androidx.room.Room
import com.mskwak.database.GardenDatabase
import com.mskwak.database.migration.MIGRATION_1_2
import com.mskwak.database.migration.MIGRATION_2_3
import com.mskwak.database.migration.MIGRATION_3_4
import com.mskwak.database.migration.MIGRATION_4_5
import com.mskwak.database.migration.MIGRATION_5_6
import com.mskwak.database.migration.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideGardenDatabase(application: Application): GardenDatabase {
        return Room
            .databaseBuilder(application, GardenDatabase::class.java, GardenDatabase.DATABASE_NAME)
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .build()
    }

    @Provides
    @Singleton
    fun providePlantDao(gardenDatabase: GardenDatabase) = gardenDatabase.plantDao()

    @Provides
    @Singleton
    fun provideDiaryDao(gardenDatabase: GardenDatabase) = gardenDatabase.diaryDao()

    @Provides
    @Singleton
    fun providePictureDao(gardenDatabase: GardenDatabase) = gardenDatabase.pictureDao()
}