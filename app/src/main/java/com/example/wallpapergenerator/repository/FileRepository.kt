package com.example.wallpapergenerator.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FileRepository @Inject constructor(private val context: Context) {
    fun saveMediaToStorage(bitmap: Bitmap) : String {
        val df: DateFormat = SimpleDateFormat("dd_MM_yyyy_hh_mm_ss")
        val date = df.format(Calendar.getInstance().time)
        val imageFileName = "wp_${date}.png"
        try {
            val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageFile = File(storageDir, imageFileName)

            val fileOutputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            MediaScannerConnection.scanFile(context, arrayOf(imageFile.absolutePath), null, null)
            return "Успешно сохранено в Pictures/"
        }
        catch (e: Exception){
            if (ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)
                return "Необходимо разрешение на доступ к мультимедиа!"
            return "Ошибка!"
        }
    }
}