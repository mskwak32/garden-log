package com.mskwak.domain.repository

import com.mskwak.domain.model.Picture

interface PictureRepository {
    suspend fun savePicture(bitmap: ByteArray): Picture
    suspend fun deletePicture(picture: Picture)
}