package com.mskwak.pdf

import android.net.Uri
import com.mskwak.pdf.model.PdfFileInfo
import com.mskwak.pdf.model.PdfRequest

interface PdfDataSource {
    suspend fun generatePdf(request: PdfRequest): Uri
    suspend fun getExportedFiles(): List<PdfFileInfo>
    suspend fun deleteFile(contentUri: Uri)
}
