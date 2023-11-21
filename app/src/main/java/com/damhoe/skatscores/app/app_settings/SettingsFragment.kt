package com.damhoe.skatscores.app.app_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentSettingsBinding
import com.damhoe.skatscores.shared_ui.utils.InsetsManager

class SettingsFragment : Fragment(), OnPreferenceClickListener {
    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)

        // Inflate the preferences screen
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(
            R.id.container,
            SettingsContentFragment.newInstance()
                .apply { setOnPreferenceClickListener(this@SettingsFragment) }
        )
        transaction.commit()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            InsetsManager.applyStatusBarInsets(appbarLayout)
            InsetsManager.applyNavigationBarInsets(nestedScrollView)

            // Setup toolbar
            val navController = findNavController()
            setupWithNavController(toolbar, navController)
        }
    }

    fun navigateToAboutFragment() {
        findNavController().navigate(SettingsFragmentDirections.actionAppSettingsToAbout())
    }

    private fun findNavController() =
        findNavController(requireActivity(), R.id.nav_host_fragment)

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference.key == "about") {
            navigateToAboutFragment()
            return true
        }
        return false
    }
}