package com.damhoe.skatscores.app.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.damhoe.skatscores.R

class ThemeProvider(
    private val context: Context
)
{

    fun getNightMode(theme: String): Int = when (theme)
    {
        context.getString(R.string.night_theme_preference_value) -> MODE_NIGHT_YES
        context.getString(R.string.day_theme_preference_value) -> MODE_NIGHT_NO
        context.getString(R.string.system_theme_preference_value) -> MODE_NIGHT_FOLLOW_SYSTEM
        // Unknown stored value: fall back to the default (dark) instead of crashing at startup.
        else -> MODE_NIGHT_YES
    }
}