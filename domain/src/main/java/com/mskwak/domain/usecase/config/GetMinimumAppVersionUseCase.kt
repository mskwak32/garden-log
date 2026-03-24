package com.mskwak.domain.useCase.config

import com.mskwak.domain.repository.AppConfigRepository

class GetMinimumAppVersionUseCase(
    private val appConfigRepository: AppConfigRepository
) {
    suspend operator fun invoke(): Int {
        return appConfigRepository.getMinimumAppVersion().getOrDefault(1)
    }
}