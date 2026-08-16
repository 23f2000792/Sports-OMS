package com.example

import android.app.Application

class SportsOpsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SportsOpsApp
            private set

        val applicationContextOrNull: Application?
            get() = if (::instance.isInitialized) instance else null
    }
}
