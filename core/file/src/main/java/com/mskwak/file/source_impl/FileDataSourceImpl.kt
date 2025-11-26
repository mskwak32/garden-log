package com.mskwak.file.source_impl

import android.app.Application
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.mskwak.file.Constants
import com.mskwak.file.FileDataSource
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

internal class FileDataSourceImpl @Inject constructor(
    application: Application
) : FileDataSource {
    private val baseDir = application.filesDir

    override fun savePicture(
        dirPath: String,
        bitmap: Bitmap
    ): File {
        val dir = File(baseDir, dirPath)

        if (!dir.exists()) {
            dir.mkdirs()
        }

        var file = File(dir, "${bitmap.hashCode()}")

        // 중복저장 피하기
        var postfix = 0
        while (file.exists()) {
            file = File(dir, "${bitmap.hashCode()}_${++postfix}.jpg")
        }

        try {
            file.createNewFile()
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.FILE_COMPRESS_QUALITY, out)
                Timber.d("save picture: ${file.toUri().path}")
            }
        } catch (e: Exception) {
            Timber.e(e, "save picture error")
        }

        return file
    }

    override fun deletePicture(path: String) {
        File(path).run {
            if (exists()) {
                delete()
                Timber.d("delete picture: $path")
            }
        }
    }
}