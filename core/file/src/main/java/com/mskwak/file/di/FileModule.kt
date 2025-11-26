package com.mskwak.file.di

import com.mskwak.file.FileDataSource
import com.mskwak.file.source_impl.FileDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FileModule {
    @Binds
    @Singleton
    abstract fun bindFileDataSource(impl: FileDataSourceImpl): FileDataSource
}