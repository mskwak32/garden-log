package com.mskwak.remote

interface AppConfigRemoteSource {
    suspend fun getLatestAppVersion(): Result<Int>
    suspend fun getUpdateContent(version: Int): Result<String>
}