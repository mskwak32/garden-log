package com.mskwak.gardendailylog.di

import com.mskwak.domain.repository.WateringAlarmRepository
import com.mskwak.gardendailylog.alarm.WateringAlarmRepositoryImpl
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
    abstract fun bindWateringAlarmRepository(
        impl: WateringAlarmRepositoryImpl
    ): WateringAlarmRepository
}