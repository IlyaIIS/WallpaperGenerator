package com.example.wallpapergenerator.repository

import android.content.Context
import javax.inject.Inject

class LocalRepository @Inject constructor(private val context: Context) {
    private val authPrefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    private val prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

    fun readToken() = authPrefs.getString(DATA_PREF_NAME, "")
    fun saveToken(token: String) = prefs.edit().putString(DATA_PREF_NAME, token).apply()
    
    fun readSettingString(name: String) = prefs.getString(name, "")
    fun readSettingInt(name: String) = prefs.getInt(name, 0)
    fun readSettingBool(name: String) : Boolean = prefs.getBoolean(name, false)

    fun saveSetting(name: String, value: String) = prefs.edit().putString(name, value).apply()
    fun saveSetting(name: String, value: Int) = prefs.edit().putInt(name, value).apply()
    fun saveSetting(name: String, value: Boolean) = prefs.edit().putBoolean(name, value).apply()

    fun resetSettings() = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE).edit().clear().commit()
    fun logout() = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit().clear().commit()

    companion object {
        const val DATA_PREF_NAME = "DATA_PREF"
        const val SHARED_PREF_NAME = "SHARED_PREF_JWT"
        const val SETTINGS_PREFS = "SETTINGS_PREFS"
    }
}