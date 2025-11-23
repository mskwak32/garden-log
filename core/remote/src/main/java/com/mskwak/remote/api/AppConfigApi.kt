package com.mskwak.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

internal interface AppConfigApi {
    @GET("appConfig/latestVersion.json")
    suspend fun getLatestAppVersion(): Response<Int>

    @GET("appConfig/debugLatestVersion.json")
    suspend fun getDebugLatestAppVersion(): Response<Int>

    @GET("appConfig/updateContent/{versionCode}.json")
    suspend fun getUpdateContent(@Path("versionCode") version: Int): Response<String>
}