package com.mskwak.remote.di

import com.mskwak.remote.AppConfigRemoteSource
import com.mskwak.remote.source_impl.AppConfigRemoteSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteSourceModule {
    @Singleton
    @Binds
    abstract fun bindAppConfigRemoteSource(impl: AppConfigRemoteSourceImpl): AppConfigRemoteSource
}