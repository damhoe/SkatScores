package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.shared.signed
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentGameGraphBinding
import com.damhoe.skatscores.databinding.ViewChartLegendItemBinding
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.plot.presentation.ScoreChartView
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint

/** How the chart reads a list: totals as they stood, or what each round was worth. */
private enum class ChartMode
{
    CUMULATIVE,
    PER_ROUND,
}

/** Points per seat over the rounds of a list, with a draggable readout. */
@AndroidEntryPoint
class GameGraphFragment : Fragment(R.layout.fragment_game_graph)
{
    /**
     * The same instance the game screen uses. A fragment-scoped view model would get its own
     * SavedStateHandle without the gameId and would have no game to draw.
     */
    private val viewModel: SharedSkatGameViewModel by hiltNavGraphViewModels(R.id.skat_game_nav_graph)

    private lateinit var binding: FragmentGameGraphBinding

    private val legendItems = mutableListOf<ViewChartLegendItemBinding>()

    private var mode = ChartMode.CUMULATIVE
    private var currentSeries: List<ScoreChartView.Series> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGameGraphBinding.bind(view)

        applyInsets()
        setupListeners()

        viewModel.skatGame.observe(viewLifecycleOwner) { game -> bindGame(game) }
    }

    private fun applyInsets()
    {
        val contentBottomPadding = binding.contentScroll.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appBar.setPadding(
                binding.appBar.paddingLeft,
                bars.top,
                binding.appBar.paddingRight,
                binding.appBar.paddingBottom
            )
            // Padding rather than a margin on the card: the content scrolls now, so the gap
            // above the navigation bar has to sit inside the scrolling area.
            binding.contentScroll.setPadding(
                binding.contentScroll.paddingLeft,
                binding.contentScroll.paddingTop,
                binding.contentScroll.paddingRight,
                contentBottomPadding + bars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupListeners()
    {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        binding.modeCumulative.setOnClickListener { setMode(ChartMode.CUMULATIVE) }
        binding.modePerRound.setOnClickListener { setMode(ChartMode.PER_ROUND) }

        binding.scoreChart.onRoundSelected = { round -> showReadoutFor(round) }
    }

    private fun setMode(mode: ChartMode)
    {
        if (this.mode == mode) return

        this.mode = mode
        binding.modeCumulative.isChecked = mode == ChartMode.CUMULATIVE
        binding.modePerRound.isChecked = mode == ChartMode.PER_ROUND

        viewModel.skatGame.value?.let { bindGame(it) }
    }

    private fun bindGame(game: SkatGame)
    {
        binding.graphTitle.text = game.title.value

        val hasRounds = game.scores.isNotEmpty()
        binding.emptyHint.visibility = if (hasRounds) View.GONE else View.VISIBLE
        binding.scoreChart.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.modeRow.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.legend.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.readoutLabel.visibility = if (hasRounds) View.VISIBLE else View.GONE

        // Nothing to break down before the first round, and nothing to break down at all
        // unless the list is scored Seeger-Fabian.
        val hasBreakdown = hasRounds && game.isTournamentScoring
        binding.breakdownSection.visibility = if (hasBreakdown) View.VISIBLE else View.GONE

        if (!hasRounds) return

        currentSeries = seriesFor(game)
        buildLegend()
        binding.scoreChart.setSeries(currentSeries)
        showReadoutFor(round = null)

        if (hasBreakdown) bindBreakdownCard(game)
    }

    /**
     * The parts of each seat's total, under the chart. The legend above shows where the list
     * stands - or the round being dragged over - so this card is the one place the numbers
     * behind that total are spelled out and add up in view.
     */
    private fun bindBreakdownCard(game: SkatGame)
    {
        val breakdowns = game.calculateBreakdown()
        val names = game.participants.asList().map { it.displayName }

        binding.apply {
            listOf(breakdownName1, breakdownName2, breakdownName3).forEachIndexed { seat, view ->
                view.text = names.getOrNull(seat).orEmpty()
                // Tied to the chart by colour, the way the legend is.
                view.setTextColor(MaterialColors.getColor(view, seriesColorAttr(seat)))
            }

            breakdownRows.bindBreakdown(breakdowns)

            listOf(breakdownTotal1, breakdownTotal2, breakdownTotal3).forEachIndexed { seat, view ->
                view.text = signed(breakdowns.getOrNull(seat)?.total ?: 0)
            }
        }
    }

    private fun seriesFor(game: SkatGame): List<ScoreChartView.Series>
    {
        val history = game.createPointsHistory()
        val seatNames = game.participants.asList().map { it.displayName }

        return history.mapIndexed { seat, totals ->
            ScoreChartView.Series(
                label = seatNames.getOrNull(seat).orEmpty(),
                color = MaterialColors.getColor(binding.scoreChart, seriesColorAttr(seat)),
                values = when (mode)
                {
                    ChartMode.CUMULATIVE -> totals
                    // What each round was worth, keeping the leading 0 so both modes share an
                    // x axis where index 0 is "before the first round".
                    ChartMode.PER_ROUND ->
                        listOf(0) + totals.zipWithNext { before, after -> after - before }
                },
            )
        }
    }

    private fun buildLegend()
    {
        if (legendItems.size != currentSeries.size)
        {
            binding.legend.removeAllViews()
            legendItems.clear()

            currentSeries.forEachIndexed { index, _ ->
                val item = ViewChartLegendItemBinding.inflate(
                    layoutInflater,
                    binding.legend,
                    true
                )
                item.root.updateLayoutParams<android.widget.LinearLayout.LayoutParams> {
                    width = 0
                    weight = 1f
                    if (index > 0) marginStart = resources.getDimensionPixelSize(R.dimen.legend_gap)
                }
                legendItems += item
            }
        }

        legendItems.forEachIndexed { index, item ->
            val series = currentSeries[index]
            item.playerName.text = series.label
            item.swatch.background?.setColorFilter(series.color, PorterDuff.Mode.SRC_IN)
            item.playerValue.setTextColor(series.color)
        }
    }

    /** Legend doubles as the readout: null shows where the list stands, a round shows that round. */
    private fun showReadoutFor(round: Int?)
    {
        binding.readoutLabel.text = when
        {
            round != null -> getString(R.string.title_round, round)
            mode == ChartMode.PER_ROUND -> getString(R.string.title_chart_per_round)
            else -> getString(R.string.title_chart_final)
        }

        legendItems.forEachIndexed { index, item ->
            val values = currentSeries.getOrNull(index)?.values.orEmpty()
            val value = round?.let { values.getOrNull(it) } ?: values.lastOrNull() ?: 0
            item.playerValue.text = signed(value)
        }
    }

    @AttrRes
    private fun seriesColorAttr(seat: Int): Int = when (seat % 3)
    {
        0 -> R.attr.colorChartSeries1
        1 -> R.attr.colorChartSeries2
        else -> R.attr.colorChartSeries3
    }

    companion object
    {
        fun newInstance() = GameGraphFragment()
    }
}