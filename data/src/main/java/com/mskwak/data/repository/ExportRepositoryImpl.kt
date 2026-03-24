package com.mskwak.data.repository

import android.net.Uri
import com.mskwak.data.mapper.toExportedFileInfo
import com.mskwak.data.mapper.toPdfRequest
import com.mskwak.domain.model.ExportRequest
import com.mskwak.domain.model.ExportedFileInfo
import com.mskwak.domain.repository.ExportRepository
import com.mskwak.pdf.PdfDataSource
import javax.inject.Inject

internal class ExportRepositoryImpl @Inject constructor(
    private val pdfDataSource: PdfDataSource
) : ExportRepository {

    override suspend fun generateExport(request: ExportRequest): String {
        return pdfDataSource.generatePdf(request.toPdfRequest()).toString()
    }

    override suspend fun getExportedFiles(): List<ExportedFileInfo> {
        return pdfDataSource.getExportedFiles().map { it.toExportedFileInfo() }
    }

    override suspend fun deleteFile(contentUri: String) {
        pdfDataSource.deleteFile(Uri.parse(contentUri))
    }
}
