package com.mskwak.remote.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mskwak.remote.Constants
import com.mskwak.remote.api.AppConfigApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class RemoteModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.APP_CONFIG_BASE_URL)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppConfigService(retrofit: Retrofit): AppConfigApi {
        return retrofit.create(AppConfigApi::class.java)
    }
}