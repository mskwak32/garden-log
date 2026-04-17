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

    override suspend fun getMinimumAppVersion(): Int {
        return runCatching {
            remoteConfig.fetchAndActivate().await()
            val key = if (BuildConfig.DEBUG) KEY_MIN_VERSION_DEBUG else KEY_MIN_VERSION_RELEASE
            remoteConfig.getLong(key).toInt().also {
                Timber.d("getMinimumAppVersion: $it")
            }
        }.onFailure {
            Timber.e(it, "getMinimumAppVersion failed")
        }.getOrDefault(DEFAULT_MIN_VERSION.toInt())
    }

    override suspend fun isFeedbackVisible(): Boolean {
        return runCatching {
            remoteConfig.fetchAndActivate().await()
            val key = if (BuildConfig.DEBUG) KEY_FEEDBACK_DEBUG else KEY_FEEDBACK_RELEASE
            remoteConfig.getBoolean(key).also {
                Timber.d("isFeedbackVisible: $it")
            }
        }.onFailure {
            Timber.e(it, "isFeedbackVisible failed")
        }.getOrDefault(DEFAULT_FEEDBACK_VISIBLE)
    }

    companion object {
        internal const val KEY_MIN_VERSION_DEBUG = "min_app_version_debug"
        internal const val KEY_MIN_VERSION_RELEASE = "min_app_version_release"
        internal const val DEFAULT_MIN_VERSION = 1L

        internal const val KEY_FEEDBACK_DEBUG = "setting_feedback_debug"
        internal const val KEY_FEEDBACK_RELEASE = "setting_feedback_release"
        internal const val DEFAULT_FEEDBACK_VISIBLE = false
    }
}
