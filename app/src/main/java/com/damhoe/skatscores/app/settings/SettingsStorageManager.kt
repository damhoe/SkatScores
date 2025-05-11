package com.damhoe.skatscores.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.damhoe.skatscores.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsStorageManager @Inject constructor(
    @ApplicationContext context: Context
)
{
    private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "skat_score_settings")

    private val storage = context.dataStore

    private val defaultTheme = context.getString(R.string.system_theme_preference_value)
    private val defaultLanguage = context.getString(R.string.german_preference_value)

    companion object
    {
        val THEME = stringPreferencesKey("themePreference")
        val LANGUAGE = stringPreferencesKey("languagePreference")
    }

    val themeFlow: Flow<String> = storage.data
        .map { preferences -> preferences[THEME] ?: defaultTheme }

    suspend fun setTheme(theme: String) = storage.edit { it[THEME] = theme }

    val languageFlow: Flow<String> = storage.data
        .map { preferences -> preferences[LANGUAGE] ?: defaultLanguage }

    suspend fun setLanguage(language: String) = storage.edit { it[LANGUAGE] = language }
}