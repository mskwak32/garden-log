package com.mskwak.domain.repository

import com.mskwak.domain.model.ExportRequest
import com.mskwak.domain.model.ExportedFileInfo

interface ExportRepository {
    suspend fun generateExport(request: ExportRequest): String
    suspend fun getExportedFiles(): List<ExportedFileInfo>
    suspend fun deleteFile(contentUri: String)
}
