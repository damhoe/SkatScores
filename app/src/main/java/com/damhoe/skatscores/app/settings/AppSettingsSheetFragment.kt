package com.damhoe.skatscores.app.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.SheetAppSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingsSheetFragment : BottomSheetDialogFragment()
{
    private lateinit var binding: SheetAppSettingsBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: AppSettingsViewModel by viewModels({ requireActivity() }) { viewModelFactory }

    @Inject
    lateinit var settingsManager: SettingsManager

    private lateinit var systemTheme: String
    private lateinit var dayTheme: String
    private lateinit var nightTheme: String
    private lateinit var german: String
    private lateinit var english: String

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        systemTheme = getString(R.string.system_theme_preference_value)
        dayTheme = getString(R.string.day_theme_preference_value)
        nightTheme = getString(R.string.night_theme_preference_value)
        german = getString(R.string.german_preference_value)
        english = getString(R.string.english_preference_value)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = SheetAppSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.closeButton.setOnClickListener { dismiss() }
        binding.aboutRow.setOnClickListener { navigateToAbout() }

        setupThemeChips()
        setupLanguageChips()
    }

    private fun setupThemeChips()
    {
        val chips = mapOf(
            dayTheme to binding.themeLight,
            nightTheme to binding.themeDark,
            systemTheme to binding.themeSystem,
        )

        chips.forEach { (value, chip) ->
            chip.setOnClickListener {
                viewModel.setTheme(value)
                checkOnly(chip, chips.values)
            }
        }

        val current = chips[viewModel.theme.value] ?: binding.themeDark
        checkOnly(current, chips.values)
    }

    private fun setupLanguageChips()
    {
        val chips = mapOf(
            german to binding.languageGerman,
            english to binding.languageEnglish,
            SettingsManager.SYSTEM_LANGUAGE to binding.languageSystem,
        )

        chips.forEach { (value, chip) ->
            chip.setOnClickListener {
                checkOnly(chip, chips.values)
                settingsManager.setLanguage(value)
            }
        }

        val current = chips[settingsManager.currentLanguage()] ?: binding.languageSystem
        checkOnly(current, chips.values)
    }

    /** The chips are independently checkable. */
    private fun checkOnly(selected: MaterialButton, all: Collection<MaterialButton>)
    {
        all.forEach { it.isChecked = it == selected }
    }

    private fun navigateToAbout()
    {
        val directions = AppSettingsSheetFragmentDirections.actionAppSettingsToAbout()
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
            .navigate(directions)
    }
}
