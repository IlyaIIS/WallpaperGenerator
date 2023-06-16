package com.example.wallpapergenerator

import androidx.lifecycle.ViewModel
import com.example.wallpapergenerator.adapters.generationsettingsadapter.SettingsParameter

sealed class ParameterHolder : ViewModel(){
    abstract fun getParameters(updateParameters: () -> Unit) : List<SettingsParameter>

    inline fun <reified T : Enum<T>> Int.toEnum(): T {
        return enumValues<T>().first { it.ordinal == this }
    }
}