package com.mskwak.data.di

import com.mskwak.data.repository.AppConfigRepositoryImpl
import com.mskwak.data.repository.DiaryRepositoryImpl
import com.mskwak.data.repository.PictureRepositoryImpl
import com.mskwak.data.repository.PlantRepositoryImpl
import com.mskwak.domain.repository.AppConfigRepository
import com.mskwak.domain.repository.DiaryRepository
import com.mskwak.domain.repository.PictureRepository
import com.mskwak.domain.repository.PlantRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPlantRepository(impl: PlantRepositoryImpl): PlantRepository

    @Binds
    @Singleton
    abstract fun bindAppConfigRepository(impl: AppConfigRepositoryImpl): AppConfigRepository

    @Binds
    @Singleton
    abstract fun bindDiaryRepository(impl: DiaryRepositoryImpl): DiaryRepository

    @Binds
    @Singleton
    abstract fun bindPictureRepository(impl: PictureRepositoryImpl): PictureRepository
}