package com.mskwak.domain.repository

interface AppConfigRepository {
    suspend fun getMinimumAppVersion(): Int
    suspend fun isFeedbackVisible(): Boolean
}