package com.damhoe.scoresheetskat.app.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.scoresheetskat.MainActivity
import com.damhoe.scoresheetskat.R
import com.damhoe.scoresheetskat.databinding.FragmentAboutBinding
import com.damhoe.scoresheetskat.shared_ui.utils.InsetsManager

class AboutFragment : Fragment() {

    lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsets()
        setUpNavigation()
    }

    private fun applyInsets() {
        binding.run {
            InsetsManager.applyStatusBarInsets(appbarLayout)
            InsetsManager.applyNavigationBarInsets(content)
        }
    }

    private fun setUpNavigation() {
        binding.run {
            val navController = findNavController()
            val appBarConfiguration = (requireActivity() as MainActivity).appBarConfiguration
            setupWithNavController(toolbar, navController, appBarConfiguration)
        }
    }

    private fun findNavController() =
        findNavController(requireActivity(), R.id.nav_host_fragment)
}