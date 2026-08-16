package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SportsOpsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("sports-ops-hub")
                    .setApplicationId("com.aistudio.sportsops.qvzkmx")
                    .setApiKey("AIzaSySportsOpsCloudSyncKey2026Secure")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("SportsOpsApp", "FirebaseApp initialized with options.")
            }
        } catch (e: Exception) {
            Log.w("SportsOpsApp", "FirebaseApp initialization note: ${e.message}")
        }
    }

    companion object {
        lateinit var instance: SportsOpsApp
            private set

        val applicationContextOrNull: Application?
            get() = if (::instance.isInitialized) instance else null
    }
}
