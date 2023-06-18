package com.example.wallpapergenerator.repository

import android.content.Context
import javax.inject.Inject

class ResourceRepository @Inject constructor(private val context: Context) {
    fun getString(id: Int) : String {
        return context.getString(id)
    }
}