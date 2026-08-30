package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.shared.signed
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentGameBinding
import com.damhoe.skatscores.game.skat.adapter.presentation.scores.RoundEntrySheetFragment
import com.damhoe.skatscores.game.skat.adapter.presentation.scores.RoundTextFactory
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class SkatGameFragment :
    Fragment(R.layout.fragment_game),
    IScoreActionListener
{
    private val viewModel: SharedSkatGameViewModel by hiltNavGraphViewModels(R.id.skat_game_nav_graph)

    private lateinit var binding: FragmentGameBinding
    private lateinit var scoreAdapter: SkatScoreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGameBinding.bind(view)

        applyInsets()
        setUpRecyclerView()
        setupAppBar()
        setupScoreboard()
        setupBottomBar()
        setupObservers()
        listenForRoundChanges()
    }

    private fun applyInsets()
    {
        val listBottomPadding = binding.scoresRv.paddingBottom
        val pillBottomMargin =
            (binding.bottomPill.layoutParams as android.view.ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBar.setPadding(
                binding.appBar.paddingLeft,
                bars.top,
                binding.appBar.paddingRight,
                binding.appBar.paddingBottom
            )
            binding.scoresRv.setPadding(
                binding.scoresRv.paddingLeft,
                binding.scoresRv.paddingTop,
                binding.scoresRv.paddingRight,
                listBottomPadding + bars.bottom
            )
            binding.bottomPill.updateLayoutParams<android.view.ViewGroup.MarginLayoutParams> {
                bottomMargin = pillBottomMargin + bars.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setUpRecyclerView()
    {
        scoreAdapter = SkatScoreAdapter(this, RoundTextFactory(requireContext()))
        binding.scoresRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scoreAdapter
        }
    }

    private fun setupAppBar()
    {
        binding.returnButton.setOnClickListener { findNavController().navigateUp() }
        binding.playersButton.setOnClickListener { showPlayerSeatsSheet() }
        binding.settingsButton.setOnClickListener { showGameSettingsSheet() }
        binding.shareButton.setOnClickListener {
            Snackbar.make(binding.root, R.string.description_share, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.bottomPill)
                .show()
        }
    }

    /** The chevron is the affordance, the whole board is the target. */
    private fun setupScoreboard()
    {
        binding.scoreboard.scoreboardCard.setOnClickListener { viewModel.toggleBreakdown() }
    }

    private fun setupBottomBar()
    {
        binding.editScoreButton.setOnClickListener { showRoundSheet(scoreId = null) }
        binding.chartAction.setOnClickListener { navigateToGameGraph() }
        // The only way to remove a round, and it only ever removes the newest one. The rows
        // themselves just edit, so this carries the confirmation the row menu used to show.
        binding.undoAction.setOnClickListener {
            viewModel.removeLastScore()
            Snackbar
                .make(binding.root, R.string.message_round_removed, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bottomPill)
                .show()
        }
    }

    private fun setupObservers()
    {
        viewModel.skatGame.observe(viewLifecycleOwner) { game ->
            binding.gameTitle.text = game.title.value
            binding.scoreboard.roundProgress.text = progressLabelFor(game)

            scoreAdapter.submitScores(game.scores)

            val hasRounds = game.scores.isNotEmpty()
            binding.emptyRoundsHint.visibility = if (hasRounds) View.GONE else View.VISIBLE
            binding.undoAction.isEnabled = hasRounds
            binding.undoAction.alpha = if (hasRounds) 1f else 0.4f

            bindScoreboard(game)
        }

        viewModel.skatParticipants.observe(viewLifecycleOwner) {
            scoreAdapter.setParticipants(it)
        }

        viewModel.isBreakdownExpanded.observe(viewLifecycleOwner) { isExpanded ->
            applyBreakdownState(
                isTournamentScoring = viewModel.skatGame.value?.isTournamentScoring == true,
                isExpanded = isExpanded,
            )
        }

        viewModel.navigateUpEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(requireView(), "Failed to load game details.", Snackbar.LENGTH_LONG)
                    .show()
                findNavController().navigateUp()
            }
        }
    }

    /** Each sheet reports back after saving so the board can pick up what changed. */
    private fun listenForRoundChanges()
    {
        listOf(
            RoundEntrySheetFragment.REQUEST_KEY,
            GameSettingsSheetFragment.REQUEST_KEY,
            PlayerSeatsSheetFragment.REQUEST_KEY,
        ).forEach { requestKey ->
            parentFragmentManager.setFragmentResultListener(
                requestKey,
                viewLifecycleOwner
            ) { _, result ->
                // The settings sheet can delete the list it was opened on; there is no game
                // left to refresh into, so leave for the library instead.
                if (result.getBoolean(GameSettingsSheetFragment.RESULT_DELETED, false))
                {
                    findNavController().navigateUp()
                    return@setFragmentResultListener
                }
                viewModel.refresh()
            }
        }
    }

    /** "Runde 6 / 24 · Gibt: Evi" — same shape as the library's active list card. */
    private fun progressLabelFor(game: SkatGame): String
    {
        val rounds = getString(
            R.string.label_round_progress,
            game.scores.size,
            game.settings.roundCount.value
        )
        val dealer = game.participants.asList()
            .getOrNull(game.dealerPosition)
            ?.let { getString(R.string.label_dealer, it.displayName) }
            ?: return rounds

        return getString(R.string.format_list_meta, rounds, dealer)
    }

    private fun bindScoreboard(game: SkatGame)
    {
        val totals = game.calculateTotalPoints()
        val breakdowns = game.calculateBreakdown()
        val names = game.participants.asList().map { it.displayName }
        val leader = totals.withIndex()
            .takeIf { game.scores.isNotEmpty() }
            ?.maxByOrNull { it.value }
            ?.index

        binding.scoreboard.apply {
            listOf(name1, name2, name3).forEachIndexed { seat, nameView ->
                nameView.text = names.getOrNull(seat).orEmpty()
            }
            listOf(total1, total2, total3).forEachIndexed { seat, totalView ->
                bindTotal(totalView, totals.getOrNull(seat) ?: 0, isLeader = seat == leader)
            }

            breakdownRows.bindBreakdown(breakdowns)
        }

        applyBreakdownState(
            isTournamentScoring = game.isTournamentScoring,
            isExpanded = viewModel.isBreakdownExpanded.value == true,
        )
    }

    private fun bindTotal(view: TextView, total: Int, isLeader: Boolean)
    {
        view.text = signed(total)
        view.setTextColor(
            MaterialColors.getColor(
                view,
                when
                {
                    total < 0 -> R.attr.colorTertiary
                    isLeader -> R.attr.colorPrimary
                    else -> R.attr.colorOnSurface
                }
            )
        )
    }

    /**
     * The breakdown belongs to tournament scoring only; in simple scoring the board stays the
     * name and the total it has always been, with nothing to expand.
     */
    private fun applyBreakdownState(isTournamentScoring: Boolean, isExpanded: Boolean)
    {
        binding.scoreboard.apply {
            breakdownToggle.visibility = if (isTournamentScoring) View.VISIBLE else View.GONE
            breakdownToggle.animate().rotation(if (isExpanded) 180f else 0f).setDuration(150).start()

            breakdown.visibility =
                if (isTournamentScoring && isExpanded) View.VISIBLE else View.GONE

            scoreboardCard.isClickable = isTournamentScoring
            // Relabels the tap without a contentDescription on the card, which would hide the
            // names and totals inside it from TalkBack.
            ViewCompat.replaceAccessibilityAction(
                scoreboardCard,
                AccessibilityActionCompat.ACTION_CLICK,
                getString(
                    if (isExpanded) R.string.description_hide_breakdown
                    else R.string.description_show_breakdown
                ),
                null,
            )
        }
    }

    private fun showRoundSheet(scoreId: UUID?)
    {
        val gameId = viewModel.skatGameId ?: return
        RoundEntrySheetFragment
            .newInstance(gameId, scoreId)
            .show(parentFragmentManager, RoundEntrySheetFragment.TAG)
    }

    private fun navigateToGameGraph()
    {
        findNavController().navigate(
            SkatGameFragmentDirections.actionSkatGameFragmentToSkatGameGraphFragment()
        )
    }

    /** The seats are their own sheet, so that a substitution is one tap from the board. */
    private fun showPlayerSeatsSheet()
    {
        val gameId = viewModel.skatGameId ?: return
        PlayerSeatsSheetFragment
            .newInstance(gameId)
            .show(parentFragmentManager, PlayerSeatsSheetFragment.TAG)
    }

    private fun showGameSettingsSheet()
    {
        val gameId = viewModel.skatGameId ?: return
        GameSettingsSheetFragment
            .newInstance(gameId)
            .show(parentFragmentManager, GameSettingsSheetFragment.TAG)
    }

    override fun notifyEdit(skatScore: SkatScore) = showRoundSheet(skatScore.id)
}
