package com.colledk.opengltest

import android.app.Application
import timber.log.Timber

class MyApplication: Application() {
    override fun onCreate() {
        Timber.plant(
            Timber.DebugTree()
        )
        super.onCreate()
    }
}