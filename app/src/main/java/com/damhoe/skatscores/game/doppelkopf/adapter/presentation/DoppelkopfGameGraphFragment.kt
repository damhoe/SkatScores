package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

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
import com.damhoe.skatscores.databinding.FragmentDoppelkopfGameGraphBinding
import com.damhoe.skatscores.databinding.ViewChartLegendItemBinding
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.plot.presentation.ScoreChartView
import com.damhoe.skatscores.shared.signed
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint

/** How the chart reads a list: totals as they stood, or what each round was worth. */
private enum class ChartMode
{
    CUMULATIVE,
    PER_ROUND,
}

/** Points per seat over the rounds of a Doppelkopf list, with a draggable readout. */
@AndroidEntryPoint
class DoppelkopfGameGraphFragment : Fragment(R.layout.fragment_doppelkopf_game_graph)
{
    /**
     * The same instance the game screen uses. A fragment-scoped view model would get its own
     * SavedStateHandle without the gameId and would have no game to draw.
     */
    private val viewModel: SharedDoppelkopfGameViewModel by hiltNavGraphViewModels(
        R.id.doppelkopf_game_nav_graph
    )

    private lateinit var binding: FragmentDoppelkopfGameGraphBinding

    private val legendItems = mutableListOf<ViewChartLegendItemBinding>()

    private var mode = ChartMode.CUMULATIVE
    private var currentSeries: List<ScoreChartView.Series> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDoppelkopfGameGraphBinding.bind(view)

        applyInsets()
        setupListeners()

        viewModel.game.observe(viewLifecycleOwner) { game -> bindGame(game) }
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

        viewModel.game.value?.let { bindGame(it) }
    }

    private fun bindGame(game: DoppelkopfGame)
    {
        binding.graphTitle.text = game.title.value

        val hasRounds = game.scores.isNotEmpty()
        binding.emptyHint.visibility = if (hasRounds) View.GONE else View.VISIBLE
        binding.scoreChart.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.modeRow.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.legend.visibility = if (hasRounds) View.VISIBLE else View.GONE
        binding.readoutLabel.visibility = if (hasRounds) View.VISIBLE else View.GONE

        if (!hasRounds) return

        currentSeries = seriesFor(game)
        buildLegend()
        binding.scoreChart.setSeries(currentSeries)
        showReadoutFor(round = null)
    }

    private fun seriesFor(game: DoppelkopfGame): List<ScoreChartView.Series>
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

    /** Four seats rather than Skat's three, so the palette has a fourth colour. */
    @AttrRes
    private fun seriesColorAttr(seat: Int): Int = when (seat % 4)
    {
        0 -> R.attr.colorChartSeries1
        1 -> R.attr.colorChartSeries2
        2 -> R.attr.colorChartSeries3
        else -> R.attr.colorChartSeries4
    }
}
