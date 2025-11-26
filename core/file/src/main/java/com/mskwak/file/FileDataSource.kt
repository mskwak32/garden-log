package com.mskwak.file

import android.graphics.Bitmap
import java.io.File

interface FileDataSource {
    fun savePicture(dirPath: String, bitmap: Bitmap): File
    fun deletePicture(path: String)
}