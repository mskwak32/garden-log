package com.mskwak.remote

interface AppConfigRemoteSource {
    suspend fun getMinimumAppVersion(): Int
    suspend fun isFeedbackVisible(): Boolean
}
