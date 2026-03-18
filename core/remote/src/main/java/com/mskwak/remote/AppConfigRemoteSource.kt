package com.mskwak.remote

interface AppConfigRemoteSource {
    suspend fun getMinimumAppVersion(): Result<Int>
}
