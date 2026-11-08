package com.damhoe.skatscores.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.damhoe.skatscores.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Theme only; the language lives in AppCompat, see [SettingsManager.currentLanguage]. */
@Singleton
class SettingsStorageManager @Inject constructor(
    @ApplicationContext context: Context
)
{
    private val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(
                name = "skat_score_settings",
                // Read before the first frame (see SkatScoresApp); a corrupt file must not crash every launch.
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
            )

    private val storage = context.dataStore

    private val defaultTheme = context.getString(R.string.night_theme_preference_value)

    companion object
    {
        val THEME = stringPreferencesKey("themePreference")
    }

    val themeFlow: Flow<String> = storage.data
        // A failed read falls back to the default theme rather than crashing startup.
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[THEME] ?: defaultTheme }

    suspend fun setTheme(theme: String) = storage.edit { it[THEME] = theme }
}
