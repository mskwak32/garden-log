package com.mskwak.domain.usecase.export

import com.mskwak.domain.repository.ExportRepository

class DeleteExportedFileUseCase(
    private val exportRepository: ExportRepository
) {
    suspend operator fun invoke(contentUri: String) {
        exportRepository.deleteFile(contentUri)
    }
}
