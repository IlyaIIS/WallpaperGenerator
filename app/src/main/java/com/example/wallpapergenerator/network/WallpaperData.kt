package com.example.wallpapergenerator.network

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData

data class WallpaperData(
    val id: Int,
    var mainColor: String,
    var likes: Int,
    var isLiked: Boolean,
    val onClick: (self: WallpaperData) -> Unit,
    val onInScreen: (self: WallpaperData) -> Unit,
) {
    val image = MutableLiveData<Bitmap?>()
    var onLiked: (self: WallpaperData) -> Unit = { }
}

data class WallpaperTextData(
    val id: Int,
    val likes: Int,
    val isLiked: Boolean,
    var mainColor: String
)
