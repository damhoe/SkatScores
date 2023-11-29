package com.damhoe.skatscores.game_statistics

import android.content.res.Resources
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NavUtils
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentGameGraphBinding
import com.damhoe.skatscores.game_statistics.GraphicUtils.dpToPx
import com.damhoe.skatscores.shared_ui.utils.InsetsManager
import com.damhoe.skatscores.shared_ui.utils.LayoutMargins

class GameGraphFragment : Fragment() {

    companion object {
        fun newInstance() = GameGraphFragment()
    }

    lateinit var binding: FragmentGameGraphBinding
    private val viewModel: GameGraphViewModel by lazy {
        ViewModelProvider(this)[GameGraphViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_game_graph, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            InsetsManager.applyStatusBarInsets(appbarLayout)
            InsetsManager.applyNavigationBarInsets(
                graphView,
                LayoutMargins(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(24f)))
        }

        NavigationUI.setupWithNavController(binding.toolbar, findNavController())
    }

    private fun findNavController() =
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
}