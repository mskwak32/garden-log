package com.mskwak.domain.useCase.config

import com.mskwak.domain.repository.AppConfigRepository

class GetLatestAppVersionUseCase(
    private val appConfigRepository: AppConfigRepository
) {
    suspend operator fun invoke(): Int? {
        return appConfigRepository.getLatestAppVersion().getOrNull()
    }
}