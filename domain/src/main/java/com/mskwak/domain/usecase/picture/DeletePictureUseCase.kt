package com.mskwak.domain.usecase.picture

import com.mskwak.domain.model.Picture
import com.mskwak.domain.repository.PictureRepository

class DeletePictureUseCase(
    private val pictureRepository: PictureRepository
) {
    suspend operator fun invoke(vararg pictures: Picture) {
        pictures.forEach {
            pictureRepository.deletePicture(it)
        }
    }
}