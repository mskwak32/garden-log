package com.mskwak.remote.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.mskwak.remote.BuildConfig
import com.mskwak.remote.source_impl.AppConfigRemoteSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class RemoteModule {
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance().apply {
            val settings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 600 else 3600
            }
            setConfigSettingsAsync(settings)
            setDefaultsAsync(
                mapOf(
                    AppConfigRemoteSourceImpl.KEY_MIN_VERSION_DEBUG to AppConfigRemoteSourceImpl.DEFAULT_MIN_VERSION,
                    AppConfigRemoteSourceImpl.KEY_MIN_VERSION_RELEASE to AppConfigRemoteSourceImpl.DEFAULT_MIN_VERSION
                )
            )
        }
    }
}
