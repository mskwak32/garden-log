package com.mskwak.plant.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.mskwak.plant.Constant
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        val bytes = context.contentResolver.openInputStream(uri)
            ?.use { it.readBytes() } ?: return null
        val rotation = getExifRotation(context, uri)
        if (rotation == 0) return bytes

        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return bytes
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(
            original, 0, 0, original.width, original.height, matrix, true
        )
        val output = ByteArrayOutputStream()
        rotated.compress(Bitmap.CompressFormat.JPEG, 100, output)
        rotated.recycle()
        original.recycle()
        output.toByteArray()
    } catch (e: Exception) {
        Timber.e(e, "Failed to read bytes from uri")
        null
    }
}

private fun getExifRotation(context: Context, uri: Uri): Int {
    return try {
        val exif = ExifInterface(
            context.contentResolver.openInputStream(uri) ?: return 0
        )
        when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to read exif orientation")
        0
    }
}

fun createCameraUri(context: Context): Uri {
    val cameraDir = context.cacheDir.resolve(Constant.CACHE_DIR_CAMERA).also { it.mkdirs() }
    val photoFile = File(cameraDir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
}

fun cleanupCameraCache(context: Context) {
    context.cacheDir.resolve(Constant.CACHE_DIR_CAMERA).listFiles()?.forEach { it.delete() }
}