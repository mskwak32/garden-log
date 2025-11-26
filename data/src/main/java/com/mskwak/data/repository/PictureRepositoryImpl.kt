package com.mskwak.data.repository

import android.graphics.BitmapFactory
import com.mskwak.domain.model.Picture
import com.mskwak.domain.repository.PictureRepository
import com.mskwak.file.FileDataSource
import javax.inject.Inject

internal class PictureRepositoryImpl @Inject constructor(
    private val fileDataSource: FileDataSource
) : PictureRepository {
    override suspend fun savePicture(bitmap: ByteArray): Picture {
        val decodedBitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)
        val file = fileDataSource.savePicture(PLANT_PICTURE_DIR, decodedBitmap)

        return Picture(
            path = file.path,
            fileName = file.name,
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun deletePicture(picture: Picture) {
        fileDataSource.deletePicture(picture.path)
    }

    companion object {
        private const val PLANT_PICTURE_DIR = "plantPicture"
    }
}