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
import com.damhoe.skatscores.player.domain.DoppelkopfPlayerStatistics
import com.damhoe.skatscores.player.domain.PlayerStatistics
import com.damhoe.skatscores.player.domain.SkatPlayerStatistics
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

        binding.listCountText.text = statistics.totalListsPlayed.toString()
        binding.gameCountText.text = statistics.totalRoundsPlayed.toString()

        bindSkat(statistics.skat)
        bindDoppelkopf(statistics.doppelkopf)
    }

    /** A game the player has never sat down to is left out rather than shown at zero. */
    private fun bindSkat(statistics: SkatPlayerStatistics)
    {
        binding.skatSection.visibility = if (statistics.hasRounds) View.VISIBLE else View.GONE
        if (!statistics.hasRounds) return

        binding.skatRounds.text = roundsSummary(statistics.roundsPlayed, statistics.listsPlayed)

        bindRate(
            rate = binding.soloShare,
            labelRes = R.string.title_stat_solo_share,
            share = statistics.soloShare,
            part = statistics.soloRoundsPlayed,
            whole = statistics.roundsPlayed,
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

    private fun bindDoppelkopf(statistics: DoppelkopfPlayerStatistics)
    {
        binding.doppelkopfSection.visibility =
            if (statistics.hasRounds) View.VISIBLE else View.GONE
        if (!statistics.hasRounds) return

        binding.doppelkopfRounds.text =
            roundsSummary(statistics.roundsPlayed, statistics.listsPlayed)

        bindRate(
            rate = binding.dkWinRate,
            labelRes = R.string.title_stat_dk_win_rate,
            share = statistics.winRate,
            part = statistics.roundsWon,
            whole = statistics.roundsPlayed,
        )
        bindRate(
            rate = binding.dkReWinRate,
            labelRes = R.string.title_stat_dk_re_win_rate,
            share = statistics.reWinRate,
            part = statistics.reRoundsWon,
            whole = statistics.reRoundsPlayed,
        )
        bindRate(
            rate = binding.dkSoloShare,
            labelRes = R.string.title_stat_dk_solo_share,
            share = statistics.soloShare,
            part = statistics.soloRoundsPlayed,
            whole = statistics.roundsPlayed,
        )
        bindRate(
            rate = binding.dkSoloWinRate,
            labelRes = R.string.title_stat_dk_solo_win_rate,
            share = statistics.soloWinRate,
            part = statistics.soloRoundsWon,
            whole = statistics.soloRoundsPlayed,
        )
    }

    /** "3 rounds in 2 lists" - both halves are plurals, so a single one still reads right. */
    private fun roundsSummary(rounds: Int, lists: Int): String
    {
        val roundsLabel =
            resources.getQuantityString(R.plurals.label_round_count, rounds, rounds)
        val listsLabel =
            resources.getQuantityString(R.plurals.label_player_list_count, lists, lists)

        return getString(R.string.format_rounds_in_lists, roundsLabel, listsLabel)
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
