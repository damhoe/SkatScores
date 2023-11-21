package com.damhoe.skatscores.app.app_settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.damhoe.skatscores.R

class SettingsContentFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    private var onPreferenceClickListener: OnPreferenceClickListener? = null

    private val themeProvider by lazy { ThemeProvider(requireContext()) }

    private val aboutPreference by lazy {
        findPreference<Preference>(getString(R.string.about_preference_key))
    }

    fun setOnPreferenceClickListener(listener: OnPreferenceClickListener) {
        onPreferenceClickListener = listener
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        aboutPreference?.onPreferenceClickListener = onPreferenceClickListener
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        when (key) {

            /* Theme settings */
            getString(R.string.theme_preference_key) -> {
                val theme = themeProvider.getTheme(
                    preferences?.getString(key, null)
                        ?: getString(R.string.system_theme_preference_value)
                    )
                AppCompatDelegate.setDefaultNightMode(theme)
            }

            /* Language settings */
            getString(R.string.language_preference_key) -> {
                val languageTag = preferences?.getString(key, null)
                    ?: getString(R.string.german_preference_value)

                val appLocales = LocaleListCompat.forLanguageTags(languageTag)
                AppCompatDelegate.setApplicationLocales(appLocales)
            }

            else -> return
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        fun newInstance(): SettingsContentFragment {
            return SettingsContentFragment()
        }
    }
}