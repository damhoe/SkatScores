package com.damhoe.skatscores.app.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
)
{

    private val themeProvider by lazy { ThemeProvider(context) }

    fun setTheme(theme: String)
    {
        val nightMode = themeProvider.getNightMode(theme)
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun setLanguage(languageTag: String)
    {
        val appLocales = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}