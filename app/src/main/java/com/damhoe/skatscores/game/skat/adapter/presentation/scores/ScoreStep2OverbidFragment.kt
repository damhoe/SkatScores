package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentScoreStep2OverbidBinding
import com.damhoe.skatscores.game.skat.adapter.presentation.SharedSkatGameViewModel
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.damhoe.skatscores.shared.utils.LayoutMargins
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScoreStep2OverbidFragment : Fragment(R.layout.fragment_score_step2_overbid)
{
    private val scoreViewModel: SharedScoreViewModel by navGraphViewModels(R.id.skat_game_nav_graph)

    private val gameViewModel: SharedSkatGameViewModel by viewModels({ requireActivity() })
    private lateinit var binding: FragmentScoreStep2OverbidBinding

    private fun findNavController() = Navigation.findNavController(
        requireActivity(), R.id.nav_host_fragment
    )

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    )
    {
        super.onViewCreated(
            view, savedInstanceState
        )
        binding = FragmentScoreStep2OverbidBinding.bind(view)

        // Setup navigation
        val navController: NavController = findNavController()
        setupWithNavController(
            binding.toolbar, navController
        )

        // Set insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout)
        val marginRight = resources.getDimensionPixelSize(R.dimen.fab_margin_right)
        val marginBottom = resources.getDimensionPixelSize(R.dimen.fab_margin_bottom)
        val defaultMargins = LayoutMargins(
            0, 0, marginRight, marginBottom
        )
        InsetsManager.applyNavigationBarInsets(
            binding.scoreDoneButton, defaultMargins
        )
        InsetsManager.applyNavigationBarInsets(binding.content)

        binding.scoreDoneButton.setOnClickListener {
            saveScore()
            findNavController().navigateUp()
        }

        initializeUI()
    }

    private fun initializeUI()
    {

    }


    private fun saveScore()
    {

    }
}