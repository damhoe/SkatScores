package com.damhoe.skatscores

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.PreferenceManager
import com.damhoe.skatscores.app.settings.AppSettingsViewModel
import com.damhoe.skatscores.app.settings.SettingsManager
import com.damhoe.skatscores.app.settings.ThemeProvider
import com.damhoe.skatscores.app.settings.ViewModelFactory
import com.damhoe.skatscores.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appComponent: ApplicationComponent
    lateinit var binding: ActivityMainBinding

    @Inject lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var factory: ViewModelFactory
    private val viewModel: AppSettingsViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize dependency injection with dagger
        appComponent = (applicationContext as SkatScoresApp).appComponent
        appComponent.inject(this)
        super.onCreate(savedInstanceState)

        viewModel.language.observe(this) { settingsManager.setLanguage(it) }
        viewModel.theme.observe(this) { settingsManager.setTheme(it) }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navController = navHostFragment!!.navController
        setupWithNavController(binding.bottomNavView, navController)
    }

    private fun loadSharedPreferences() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // UI mode
        val theme = sharedPreferences.getString(
            getString(R.string.theme_preference_key),
            getString(R.string.day_theme_preference_value)
        )
        AppCompatDelegate.setDefaultNightMode(ThemeProvider(this).getNightMode(theme!!))

        // System Locale
        val language = sharedPreferences.getString(
            getString(R.string.language_preference_key),
            getString(R.string.german_preference_value)
        )
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"))
        Log.d("Loaded Preference", "UI mode: $theme")
        Log.d("Loaded Preference", "Language: $language")
    }

    val appBarConfiguration: AppBarConfiguration
        get() = AppBarConfiguration.Builder(findNavController().graph).build()

    private fun findNavController(): NavController {
        return findNavController(this, R.id.nav_host_fragment)
    }

    override fun onSupportNavigateUp(): Boolean {
        return (navigateUp(findNavController(), appBarConfiguration)
                || super.onSupportNavigateUp())
    }
}