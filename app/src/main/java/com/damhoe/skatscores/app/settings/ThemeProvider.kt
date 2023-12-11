package com.damhoe.skatscores.app.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.damhoe.skatscores.R
import java.lang.IllegalArgumentException

class ThemeProvider(private val context: Context) {

    fun getNightMode(theme: String): Int = when (theme) {
        context.getString(R.string.night_theme_preference_value) -> MODE_NIGHT_YES
        context.getString(R.string.day_theme_preference_value) -> MODE_NIGHT_NO
        context.getString(R.string.system_theme_preference_value) -> MODE_NIGHT_FOLLOW_SYSTEM
        else -> throw IllegalArgumentException("Theme not defined for $theme")
    }
}