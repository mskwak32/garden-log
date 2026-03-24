package com.mskwak.pdf.di

import com.mskwak.pdf.PdfDataSource
import com.mskwak.pdf.source_impl.PdfDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PdfModule {
    @Binds
    @Singleton
    abstract fun bindPdfDataSource(impl: PdfDataSourceImpl): PdfDataSource
}
