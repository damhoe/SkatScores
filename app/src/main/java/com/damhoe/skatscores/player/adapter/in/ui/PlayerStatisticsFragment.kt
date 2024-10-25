package com.damhoe.skatscores.player.adapter.`in`.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.damhoe.skatscores.MainActivity
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentPlayerStatisticsBinding
import com.damhoe.skatscores.player.domain.PlayerStatistics
import com.damhoe.skatscores.shared.shared_ui.utils.InsetsManager
import javax.inject.Inject
import kotlin.math.roundToInt

class PlayerStatisticsFragment : Fragment() {

    @Inject
    lateinit var statisticsVMFactory: PlayerStatisticsViewModelFactory
    private val statsViewModel: PlayerStatisticsViewModel by viewModels { statisticsVMFactory }

    @Inject
    lateinit var playerVMFactory: PlayerViewModelFactory
    private val playerViewModel: PlayerViewModel by viewModels({ requireActivity() }) { playerVMFactory }

    private lateinit var binding: FragmentPlayerStatisticsBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_player_statistics, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add insets
        InsetsManager.applyStatusBarInsets(binding.appbarLayout)
        InsetsManager.applyNavigationBarInsets(binding.nestedScrollView)
        setupWithNavController(binding.toolbar, findNavController())

        playerViewModel.selectedPlayer.observe(viewLifecycleOwner) {
            val stats = statsViewModel.loadPlayerStatistics(playerId = it.id)
            updateUI(it.name, stats)
        }

        // Init
        playerViewModel.selectedPlayer.value?.let {
            val stats = statsViewModel.loadPlayerStatistics(playerId = it.id)
            updateUI(it.name, stats)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(name: String, playerStatistics: PlayerStatistics) {
        binding.apply {
            this.name.text = name

            listCountText.text = playerStatistics.listCount.toString()
            gameCountText.text = playerStatistics.gamesCount.toString()

            soloIndicator.progress = playerStatistics.soloPercentage.toInt()
            winsIndicator.progress = playerStatistics.soloWinPercentage.toInt()
            againstIndicator.progress = playerStatistics.againstWinPercentage.toInt()

            soloText.text = "${playerStatistics.soloPercentage.roundToInt()}%"
            winsText.text = "${playerStatistics.soloWinPercentage.roundToInt()}%"
            againstText.text = "${playerStatistics.againstWinPercentage.roundToInt()}%"
        }
    }

    private fun findNavController(): NavController {
        return findNavController(requireActivity(), R.id.nav_host_fragment)
    }
}