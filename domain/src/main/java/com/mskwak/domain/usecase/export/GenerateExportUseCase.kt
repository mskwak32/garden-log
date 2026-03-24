package com.mskwak.domain.usecase.export

import com.mskwak.domain.model.ExportRequest
import com.mskwak.domain.repository.ExportRepository

class GenerateExportUseCase(
    private val exportRepository: ExportRepository
) {
    suspend operator fun invoke(request: ExportRequest): String {
        return exportRepository.generateExport(request)
    }
}
