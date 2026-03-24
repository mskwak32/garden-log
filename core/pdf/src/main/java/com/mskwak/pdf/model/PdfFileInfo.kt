package com.mskwak.pdf.model

import android.net.Uri

data class PdfFileInfo(
    val contentUri: Uri,
    val fileName: String,
    val createdAt: Long,
    val fileSize: Long
)
