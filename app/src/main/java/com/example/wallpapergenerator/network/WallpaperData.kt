package com.example.wallpapergenerator.network

import android.graphics.Bitmap

data class WallpaperData(
    val id: Int,
    val image: Bitmap,
    val likes: Int,
)

data class WallpaperTextData(
    val id: Int,
    val likes: Int,
)
