package com.mskwak.remote.source_impl

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mskwak.remote.AppConfigRemoteSource
import com.mskwak.remote.BuildConfig
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

internal class AppConfigRemoteSourceImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : AppConfigRemoteSource {

    override suspend fun getMinimumAppVersion(): Result<Int> {
        return runCatching {
            remoteConfig.fetchAndActivate().await()
            val key = if (BuildConfig.DEBUG) KEY_MIN_VERSION_DEBUG else KEY_MIN_VERSION_RELEASE
            remoteConfig.getLong(key).toInt().also {
                Timber.d("getMinimumAppVersion: $it")
            }
        }.onFailure {
            Timber.e(it, "getMinimumAppVersion failed")
        }
    }

    companion object {
        internal const val KEY_MIN_VERSION_DEBUG = "min_app_version_debug"
        internal const val KEY_MIN_VERSION_RELEASE = "min_app_version_release"
        internal const val DEFAULT_MIN_VERSION = 1L
    }
}
