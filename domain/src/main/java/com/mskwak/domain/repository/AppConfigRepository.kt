package com.mskwak.domain.repository

interface AppConfigRepository {
    suspend fun getMinimumAppVersion(): Result<Int>
}