package com.ldlywt.commoncode

import android.app.Application

val applicationContext = App.instance

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }
}