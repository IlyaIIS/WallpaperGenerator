package com.example.wallpapergenerator.repository

import android.content.Context
import android.widget.Toast
import javax.inject.Inject

class ToastMessageDrawer @Inject constructor(private val context: Context) {
    fun showMessage(text : String){
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(context, text, duration)
        toast.show()
    }
}