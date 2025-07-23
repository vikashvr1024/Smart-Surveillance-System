package com.app.smart

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SmartApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Restore dark mode setting
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val nightMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
} 