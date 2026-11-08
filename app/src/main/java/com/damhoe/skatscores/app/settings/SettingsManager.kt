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

    /**
     * A language tag or [SYSTEM_LANGUAGE]. AppCompat is the only store, so this stays in sync
     * with the system's per-app language setting.
     */
    fun currentLanguage(): String = AppCompatDelegate.getApplicationLocales()
        .get(0)
        ?.language
        ?: SYSTEM_LANGUAGE

    fun setLanguage(languageTag: String)
    {
        val appLocales =
            if (languageTag == SYSTEM_LANGUAGE) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    companion object
    {
        const val SYSTEM_LANGUAGE = ""
    }
}
