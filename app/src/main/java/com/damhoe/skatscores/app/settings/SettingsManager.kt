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
     * The app's language as a language tag ("de", "en"), or empty when it follows the system.
     *
     * AppCompat is the one place this is kept: on Android 13+ it is the per-app language the
     * system settings show and change, and below that AppCompat stores it itself. The app used
     * to keep its own copy, defaulting to German, and pushed it on every start - so an English
     * phone opened in German, and a language picked in the system settings was overwritten on
     * the next launch.
     */
    fun currentLanguage(): String = AppCompatDelegate.getApplicationLocales()
        .get(0)
        ?.language
        .orEmpty()

    /** [languageTag] empty follows the system language again. */
    fun setLanguage(languageTag: String)
    {
        val appLocales =
            if (languageTag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}
