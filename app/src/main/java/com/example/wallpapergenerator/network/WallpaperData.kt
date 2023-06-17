package com.example.wallpapergenerator.network

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class WallpaperData(
    val id: Int,
    val likes: Int,
    val onClick: (self: WallpaperData) -> Unit,
    val onInScreen: (self: WallpaperData) -> Unit
) {
    val image = MutableLiveData<Bitmap?>()
}

data class WallpaperTextData(
    val id: Int,
    val likes: Int,
)
