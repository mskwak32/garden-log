package com.mskwak.data.repository

import com.mskwak.domain.repository.AppConfigRepository
import com.mskwak.remote.AppConfigRemoteSource
import javax.inject.Inject

internal class AppConfigRepositoryImpl @Inject constructor(
    private val appConfigRemoteSource: AppConfigRemoteSource
) : AppConfigRepository {
    override suspend fun getLatestAppVersion(): Result<Int> {
        return appConfigRemoteSource.getLatestAppVersion()
    }

    override suspend fun getUpdateContent(versionCode: Int): Result<String> {
        return appConfigRemoteSource.getUpdateContent(versionCode)
    }
}
