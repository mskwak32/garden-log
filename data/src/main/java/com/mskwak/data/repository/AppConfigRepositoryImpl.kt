package com.mskwak.data.repository

import com.mskwak.domain.repository.AppConfigRepository
import com.mskwak.remote.AppConfigRemoteSource
import javax.inject.Inject

internal class AppConfigRepositoryImpl @Inject constructor(
    private val appConfigRemoteSource: AppConfigRemoteSource
) : AppConfigRepository {
    override suspend fun getMinimumAppVersion(): Result<Int> {
        return appConfigRemoteSource.getMinimumAppVersion()
    }
}