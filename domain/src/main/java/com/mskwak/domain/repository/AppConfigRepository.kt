package com.mskwak.domain.repository

interface AppConfigRepository {
    suspend fun getLatestAppVersion(): Result<Int>
    suspend fun getUpdateContent(versionCode: Int): Result<String>
}