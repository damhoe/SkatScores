package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentDoppelkopfGameBinding
import com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores.DoppelkopfRoundEntrySheetFragment
import com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores.DoppelkopfRoundTextFactory
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.shared.signed
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class DoppelkopfGameFragment :
    Fragment(R.layout.fragment_doppelkopf_game),
    DoppelkopfScoreActionListener
{
    private val viewModel: SharedDoppelkopfGameViewModel by hiltNavGraphViewModels(
        R.id.doppelkopf_game_nav_graph
    )

    private lateinit var binding: FragmentDoppelkopfGameBinding
    private lateinit var scoreAdapter: DoppelkopfScoreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDoppelkopfGameBinding.bind(view)

        applyInsets()
        setUpRecyclerView()
        setupAppBar()
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
        scoreAdapter = DoppelkopfScoreAdapter(this, DoppelkopfRoundTextFactory(requireContext()))
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
        viewModel.game.observe(viewLifecycleOwner) { game ->
            binding.gameTitle.text = game.title.value
            binding.scoreboard.roundProgress.text = progressLabelFor(game)

            scoreAdapter.submitScores(game.scores) { game.valueOf(it) }

            val hasRounds = game.scores.isNotEmpty()
            binding.emptyRoundsHint.visibility = if (hasRounds) View.GONE else View.VISIBLE
            binding.undoAction.isEnabled = hasRounds
            binding.undoAction.alpha = if (hasRounds) 1f else 0.4f

            bindScoreboard(game)
        }

        viewModel.participants.observe(viewLifecycleOwner) {
            scoreAdapter.setParticipants(it)
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
            DoppelkopfRoundEntrySheetFragment.REQUEST_KEY,
            DoppelkopfGameSettingsSheetFragment.REQUEST_KEY,
            DoppelkopfPlayerSeatsSheetFragment.REQUEST_KEY,
        ).forEach { requestKey ->
            parentFragmentManager.setFragmentResultListener(
                requestKey,
                viewLifecycleOwner
            ) { _, result ->
                // The settings sheet can delete the list it was opened on; there is no game
                // left to refresh into, so leave for the library instead.
                if (result.getBoolean(DoppelkopfGameSettingsSheetFragment.RESULT_DELETED, false))
                {
                    findNavController().navigateUp()
                    return@setFragmentResultListener
                }
                viewModel.refresh()
            }
        }
    }

    /** "Runde 9 / 16 · Gibt: Evi" — same shape as the library's active list card. */
    private fun progressLabelFor(game: DoppelkopfGame): String
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

    private fun bindScoreboard(game: DoppelkopfGame)
    {
        val records = game.calculateRecords()
        val names = game.participants.asList().map { it.displayName }
        val leader = records.withIndex()
            .takeIf { game.scores.isNotEmpty() }
            ?.maxByOrNull { it.value.total }
            ?.index

        binding.scoreboard.apply {
            listOf(name1, name2, name3, name4).forEachIndexed { seat, nameView ->
                nameView.text = names.getOrNull(seat).orEmpty()
            }
            listOf(total1, total2, total3, total4).forEachIndexed { seat, totalView ->
                bindTotal(
                    totalView,
                    records.getOrNull(seat)?.total ?: 0,
                    isLeader = seat == leader,
                )
            }
        }
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

    private fun showRoundSheet(scoreId: UUID?)
    {
        val gameId = viewModel.gameId ?: return
        DoppelkopfRoundEntrySheetFragment
            .newInstance(gameId, scoreId)
            .show(parentFragmentManager, DoppelkopfRoundEntrySheetFragment.TAG)
    }

    private fun navigateToGameGraph()
    {
        findNavController().navigate(
            DoppelkopfGameFragmentDirections
                .actionDoppelkopfGameFragmentToDoppelkopfGameGraphFragment()
        )
    }

    /** The seats are their own sheet, so that a substitution is one tap from the board. */
    private fun showPlayerSeatsSheet()
    {
        val gameId = viewModel.gameId ?: return
        DoppelkopfPlayerSeatsSheetFragment
            .newInstance(gameId)
            .show(parentFragmentManager, DoppelkopfPlayerSeatsSheetFragment.TAG)
    }

    private fun showGameSettingsSheet()
    {
        val gameId = viewModel.gameId ?: return
        DoppelkopfGameSettingsSheetFragment
            .newInstance(gameId)
            .show(parentFragmentManager, DoppelkopfGameSettingsSheetFragment.TAG)
    }

    override fun notifyEdit(score: DoppelkopfScore) = showRoundSheet(score.id)
}
