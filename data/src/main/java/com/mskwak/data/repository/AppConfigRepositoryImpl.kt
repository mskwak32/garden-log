package com.mskwak.data.repository

import com.mskwak.domain.repository.AppConfigRepository
import com.mskwak.remote.AppConfigRemoteSource
import javax.inject.Inject

internal class AppConfigRepositoryImpl @Inject constructor(
    private val appConfigRemoteSource: AppConfigRemoteSource
) : AppConfigRepository {
    override suspend fun getMinimumAppVersion(): Int {
        return appConfigRemoteSource.getMinimumAppVersion()
    }

    override suspend fun isFeedbackVisible(): Boolean {
        return appConfigRemoteSource.isFeedbackVisible()
    }
}