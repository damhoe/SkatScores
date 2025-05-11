package com.damhoe.skatscores

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import com.damhoe.skatscores.app.settings.AppSettingsViewModel
import com.damhoe.skatscores.app.settings.SettingsManager
import com.damhoe.skatscores.app.settings.ViewModelFactory
import com.damhoe.skatscores.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity()
{

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var factory: ViewModelFactory
    private val viewModel: AppSettingsViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        viewModel.language.observe(this) { settingsManager.setLanguage(it) }
        viewModel.theme.observe(this) { settingsManager.setTheme(it) }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navController = navHostFragment!!.navController
    }

    val appBarConfiguration: AppBarConfiguration
        get() = AppBarConfiguration.Builder(findNavController().graph).build()

    private fun findNavController(): NavController
    {
        return findNavController(this, R.id.nav_host_fragment)
    }

    override fun onSupportNavigateUp(): Boolean
    {
        return (navigateUp(findNavController(), appBarConfiguration)
                || super.onSupportNavigateUp())
    }
}