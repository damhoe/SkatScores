package com.damhoe.skatscores.app.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.damhoe.skatscores.MainActivity
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.DialogAppSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class AppSettingsDialogFragment : DialogFragment() {
    /**
     * Dialog fragment for managing app settings.
     *
     * Settings are stored using preference data store, which is
     * managed by the view model.
     * Settings can be applied using the settings manager.
     *
     * Relevant properties are initialized in onAttach() when
     * the calling context is attached to this fragment.
     */

    // Languages
    private lateinit var german: String
    private lateinit var english: String

    // Theme options
    private lateinit var systemTheme: String
    private lateinit var dayTheme: String
    private lateinit var nightTheme: String

    private lateinit var binding: DialogAppSettingsBinding
    private lateinit var dialog: AlertDialog
    private lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: AppSettingsViewModel by viewModels ({ requireActivity() }) { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).appComponent.inject(this)

        settingsManager = SettingsManager(context)

        systemTheme = getString(R.string.system_theme_preference_value)
        dayTheme = getString(R.string.day_theme_preference_value)
        nightTheme = getString(R.string.night_theme_preference_value)

        german = getString(R.string.german_preference_value)
        english = getString(R.string.english_preference_value)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let{
            binding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.dialog_app_settings,
                null,
                false
            )

            dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(
                    R.string.title_settings)
                .setView(
                    binding.root)
                .setPositiveButton("OK") { d, _  -> d.dismiss() }
                .setBackground(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.background_dialog_fragment,
                        requireActivity().theme))
                .create()

        } ?: throw IllegalStateException("Activity can not be null")

        // Add listeners
        binding.buttonAbout.setOnClickListener {
            val directions = AppSettingsDialogFragmentDirections
                .actionAppSettingsToAbout()
            findNavController().navigate(directions)
        }

        // Initialize views
        setDefaultTheme(viewModel.theme.value)
        setDefaultLanguage(viewModel.language.value)

        addThemeOnCheckedChangeListener()
        addLanguageOnCheckedChangeListener()

        // Return dialog
        return dialog
    }

    private fun setDefaultTheme(theme: String?) {
        binding.themeGroup.check(
            when(theme) {
                dayTheme -> R.id.buttonLightTheme
                nightTheme -> R.id.buttonDarkTheme
                else -> R.id.buttonSystemTheme // Default
            }
        )
    }

    private fun setDefaultLanguage(language: String?) {
        binding.languageGroup.check(
            when(language) {
                english -> R.id.buttonEnglishLanguage
                else -> R.id.buttonGermanLanguage // Default german
            }
        )
    }

    private fun addThemeOnCheckedChangeListener() {
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.buttonSystemTheme -> systemTheme
                R.id.buttonLightTheme -> dayTheme
                R.id.buttonDarkTheme -> nightTheme
                else -> throw IllegalArgumentException(
                    "Unexpected checkedId: $checkedId")
            }.let { viewModel.setTheme(it) }
        }
    }

    private fun addLanguageOnCheckedChangeListener() {
        binding.languageGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.buttonEnglishLanguage -> english
                R.id.buttonGermanLanguage -> german
                else -> throw IllegalArgumentException(
                    "Unexpected checkedId: $checkedId")
            }.let { viewModel.setLanguage(it) }
        }
    }

    private fun findNavController() =
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
}