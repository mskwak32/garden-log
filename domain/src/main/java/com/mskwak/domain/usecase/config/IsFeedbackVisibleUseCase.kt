package com.mskwak.domain.usecase.config

import com.mskwak.domain.repository.AppConfigRepository

class IsFeedbackVisibleUseCase(
    private val appConfigRepository: AppConfigRepository
) {
    suspend operator fun invoke(): Boolean = appConfigRepository.isFeedbackVisible()
}
