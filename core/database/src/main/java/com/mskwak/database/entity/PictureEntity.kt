package com.mskwak.database.entity

import kotlinx.serialization.Serializable

@Serializable
data class PictureEntity(
    val path: String,
    val fileName: String,
    val createdAt: Long
)
