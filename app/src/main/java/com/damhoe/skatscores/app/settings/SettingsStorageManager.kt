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

/** Theme only; the language lives in AppCompat, see [SettingsManager.currentLanguage]. */
@Singleton
class SettingsStorageManager @Inject constructor(
    @ApplicationContext context: Context
)
{
    private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "skat_score_settings")

    private val storage = context.dataStore

    private val defaultTheme = context.getString(R.string.night_theme_preference_value)

    companion object
    {
        val THEME = stringPreferencesKey("themePreference")
    }

    val themeFlow: Flow<String> = storage.data
        .map { preferences -> preferences[THEME] ?: defaultTheme }

    suspend fun setTheme(theme: String) = storage.edit { it[THEME] = theme }
}
