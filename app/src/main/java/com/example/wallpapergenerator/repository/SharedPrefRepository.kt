package com.example.wallpapergenerator.repository

import android.content.Context
import javax.inject.Inject

class SharedPrefRepository @Inject constructor(context: Context) {
    private val prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    fun readData() = prefs.getString(DATA_PREF_NAME, "")

    fun saveData(str: String) = prefs.edit().putString(DATA_PREF_NAME, str).apply()


    companion object {
        const val DATA_PREF_NAME = "DATA_PREF"
        const val SHARED_PREF_NAME = "SHARED_PREF_JWT"
    }

}