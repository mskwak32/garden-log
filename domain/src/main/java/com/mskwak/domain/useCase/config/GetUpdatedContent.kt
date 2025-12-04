package com.mskwak.domain.useCase.config

import com.mskwak.domain.repository.AppConfigRepository

class GetUpdatedContent(
    val appConfigRepository: AppConfigRepository
) {
    suspend operator fun invoke(versionCode: Int): String? {
        return appConfigRepository.getUpdateContent(versionCode).getOrNull()?.run {
            replace("\\\n", "\n\n")
            replace("\"", "")
        }
    }
}