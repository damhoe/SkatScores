package com.damhoe.skatscores

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
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
        // Before super.onCreate, so the first frame already uses the stored theme.
        (application as SkatScoresApp).applyStoredTheme()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.theme.observe(this) {
            settingsManager.setTheme(it)
        }
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
