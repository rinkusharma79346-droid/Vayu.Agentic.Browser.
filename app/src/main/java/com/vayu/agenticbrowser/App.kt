package com.vayu.agenticbrowser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.vayu.agenticbrowser.common.Logger

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.i("VAYU Agentic Browser initialized")
    }
}
