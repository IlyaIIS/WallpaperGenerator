package com.example.wallpapergenerator.di

import android.app.Application

class MainApplication : Application() {
    lateinit var appComponent: MainComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerMainComponent.factory().create(this)
    }
}