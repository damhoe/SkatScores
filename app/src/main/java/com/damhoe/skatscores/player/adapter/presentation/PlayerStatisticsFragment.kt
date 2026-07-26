package com.damhoe.skatscores.player.adapter.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentPlayerStatisticsBinding
import com.damhoe.skatscores.player.domain.PlayerStatistics
import com.damhoe.skatscores.shared.utils.InsetsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class PlayerStatisticsFragment :
    Fragment(R.layout.fragment_player_statistics)
{
    private val statsViewModel: StatisticsViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    private lateinit var binding: FragmentPlayerStatisticsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPlayerStatisticsBinding.bind(view)

        // Add insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout)
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView)
        setupWithNavController(binding.toolbar, findNavController())
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(name: String, playerStatistics: PlayerStatistics)
    {
        binding.apply {
            this.name.text = name

            listCountText.text = playerStatistics.totalGamesPlayed.toString()
            gameCountText.text = playerStatistics.totalRoundsPlayed.toString()

            soloIndicator.progress = playerStatistics.soloPercentage.toInt()
            winsIndicator.progress = playerStatistics.soloWinPercentage.toInt()
            againstIndicator.progress = playerStatistics.opponentWinPercentage.toInt()

            soloText.text = "${playerStatistics.soloPercentage.roundToInt()}%"
            winsText.text = "${playerStatistics.soloWinPercentage.roundToInt()}%"
            againstText.text = "${playerStatistics.opponentWinPercentage.roundToInt()}%"
        }
    }

    private fun findNavController(): NavController
    {
        return findNavController(
            requireActivity(),
            R.id.nav_host_fragment
        )
    }
}