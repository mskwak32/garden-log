package com.mskwak.domain.model

data class ExportedFileInfo(
    val contentUri: String,
    val fileName: String,
    val createdAt: Long,
    val fileSize: Long
)
