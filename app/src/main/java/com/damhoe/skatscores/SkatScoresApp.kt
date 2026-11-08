package com.damhoe.skatscores

import android.app.Application
import com.damhoe.skatscores.app.settings.SettingsManager
import com.damhoe.skatscores.app.settings.SettingsStorageManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class SkatScoresApp :
    Application()
{
    @Inject
    lateinit var settingsStorageManager: SettingsStorageManager

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate()
    {
        super.onCreate()

        // Before the first activity: applied later, a theme other than the system's would flash
        // the system's first and then recreate the activity on every cold start.
        val theme = runBlocking { settingsStorageManager.themeFlow.first() }
        settingsManager.setTheme(theme)
    }
}
