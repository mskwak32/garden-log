package com.mskwak.domain.model

data class Picture(
    val path: String,
    val fileName: String? = null,
    val createdAt: Long? = null
)
