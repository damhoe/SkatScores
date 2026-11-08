package com.damhoe.skatscores

import android.app.Application
import com.damhoe.skatscores.app.settings.SettingsManager
import com.damhoe.skatscores.app.settings.SettingsStorageManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    private lateinit var storedTheme: Deferred<String>
    private var themeApplied = false

    override fun onCreate()
    {
        super.onCreate()

        // Off the main thread, so the read overlaps the rest of startup instead of blocking it.
        storedTheme = CoroutineScope(Dispatchers.IO).async {
            settingsStorageManager.themeFlow.first()
        }
    }

    /**
     * Called by [MainActivity] before its first frame: applied later, a theme other than the
     * system's would flash the system's first and then recreate the activity on every cold start.
     * The read started in [onCreate], so this rarely has to wait.
     */
    fun applyStoredTheme()
    {
        if (themeApplied) return
        settingsManager.setTheme(runBlocking { storedTheme.await() })
        themeApplied = true
    }
}
