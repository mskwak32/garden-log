package com.mskwak.remote.source_impl

import com.mskwak.remote.AppConfigRemoteSource
import com.mskwak.remote.BuildConfig
import com.mskwak.remote.api.AppConfigApi
import timber.log.Timber
import javax.inject.Inject

internal class AppConfigRemoteSourceImpl @Inject constructor(
    private val appConfigApi: AppConfigApi
) : AppConfigRemoteSource {
    override suspend fun getLatestAppVersion(): Result<Int> {
        return runCatching {
            val response = if (BuildConfig.DEBUG) {
                appConfigApi.getDebugLatestAppVersion()
            } else {
                appConfigApi.getLatestAppVersion()
            }
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Response body is null")
            } else {
                val exception = Exception(response.errorBody()?.string())
                Timber.e(exception)
                throw exception
            }
        }
    }

    override suspend fun getUpdateContent(version: Int): Result<String> {
        return runCatching {
            val response = appConfigApi.getUpdateContent(version)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body != "null") {
                    body
                } else {
                    throw Exception("Update content is null or invalid")
                }
            } else {
                val exception = Exception(response.errorBody()?.string())
                Timber.e(exception)
                throw exception
            }
        }
    }

}
