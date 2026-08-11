package com.damhoe.skatscores.player.adapter.presentation

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentPlayerStatisticsBinding
import com.damhoe.skatscores.databinding.ViewStatRateBinding
import com.damhoe.skatscores.player.domain.PlayerStatistics
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class PlayerStatisticsFragment : Fragment(R.layout.fragment_player_statistics)
{
    private val viewModel: StatisticsViewModel by viewModels()

    private lateinit var binding: FragmentPlayerStatisticsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayerStatisticsBinding.bind(view)

        applyInsets()
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        viewModel.playerName.observe(viewLifecycleOwner) { binding.name.text = it }
        viewModel.statistics.observe(viewLifecycleOwner) { updateUi(it) }
    }

    private fun applyInsets()
    {
        val contentBottomPadding = binding.content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBar.setPadding(
                binding.appBar.paddingLeft,
                bars.top,
                binding.appBar.paddingRight,
                binding.appBar.paddingBottom
            )
            binding.content.setPadding(
                binding.content.paddingLeft,
                binding.content.paddingTop,
                binding.content.paddingRight,
                contentBottomPadding + bars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateUi(statistics: PlayerStatistics?)
    {
        // Rates need rounds behind them to mean anything.
        val hasRounds = statistics != null && statistics.hasRounds
        binding.statsContent.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.emptyHint.visibility = if (hasRounds) View.GONE else View.VISIBLE

        if (statistics == null || !hasRounds) return

        binding.listCountText.text = statistics.totalGamesPlayed.toString()
        binding.gameCountText.text = statistics.totalRoundsPlayed.toString()

        bindRate(
            rate = binding.soloShare,
            labelRes = R.string.title_stat_solo_share,
            share = statistics.soloShare,
            part = statistics.soloRoundsPlayed,
            whole = statistics.totalRoundsPlayed,
        )
        bindRate(
            rate = binding.soloWinRate,
            labelRes = R.string.title_stat_solo_win_rate,
            share = statistics.soloWinRate,
            part = statistics.soloRoundsWon,
            whole = statistics.soloRoundsPlayed,
        )
        bindRate(
            rate = binding.defenderWinRate,
            labelRes = R.string.title_stat_defender_win_rate,
            share = statistics.defenderWinRate,
            part = statistics.roundsWonAsOpponent,
            whole = statistics.opponentRoundsPlayed,
        )
    }

    private fun bindRate(
        rate: ViewStatRateBinding,
        @StringRes labelRes: Int,
        share: Double,
        part: Int,
        whole: Int,
    )
    {
        val percent = (share * 100).roundToInt()

        rate.rateLabel.setText(labelRes)
        rate.ratePercent.text = getString(R.string.format_percent, percent)
        rate.rateBar.setProgressCompat(percent, /* animated = */ true)
        rate.rateDetail.text = getString(R.string.format_stat_of, part, whole)
    }
}
