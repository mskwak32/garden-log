package com.mskwak.domain.useCase.picture

import com.mskwak.domain.model.Picture
import com.mskwak.domain.repository.PictureRepository

class SavePictureUseCase(
    private val pictureRepository: PictureRepository
) {
    suspend operator fun invoke(bitmap: ByteArray): Picture {
        return pictureRepository.savePicture(bitmap)
    }
}