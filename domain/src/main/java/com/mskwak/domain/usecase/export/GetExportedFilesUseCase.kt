package com.mskwak.domain.usecase.export

import com.mskwak.domain.model.ExportedFileInfo
import com.mskwak.domain.repository.ExportRepository

class GetExportedFilesUseCase(
    private val exportRepository: ExportRepository
) {
    suspend operator fun invoke(): List<ExportedFileInfo> {
        return exportRepository.getExportedFiles()
    }
}
