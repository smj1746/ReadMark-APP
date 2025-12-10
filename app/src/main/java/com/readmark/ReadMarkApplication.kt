package com.readmark

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

@HiltAndroidApp
class ReadMarkApplication : Application() {

    companion object {
        private const val TAG = "ReadMarkApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "ReadMark application started")

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Running in debug mode")
        }
    }
}
