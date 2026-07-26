package com.damhoe.skatscores.game.skat.adapter.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.FragmentGameGraphBinding
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.plot.presentation.GraphicUtils.dpToPx
import com.damhoe.skatscores.shared.utils.InsetsManager
import com.damhoe.skatscores.shared.utils.LayoutMargins
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class GameGraphFragment : Fragment()
{

    companion object
    {
        fun newInstance() = GameGraphFragment()
    }

    private val viewModel: SharedSkatGameViewModel by viewModels()

    lateinit var binding: FragmentGameGraphBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_game_graph, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            InsetsManager.applyStatusBarInsets(toolbar)
            InsetsManager.applyNavigationBarInsets(
                graphView,
                LayoutMargins(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(24f))
            )
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        //viewModel.game.observe(viewLifecycleOwner) { it?.let { updateScorePlot(it) } }
    }

    private fun updateScorePlot(
        game: SkatGame
    )
    {
//        val pointsHistory = TotalPointsCalculator
//            .createPointsHistory(
//                game.players.size,
//                game.scores,
//                game.settings.isTournamentScoring
//            )

//        val allPoints = pointsHistory.flatten()
//        val minPoints = allPoints.min()
//        val maxPoints = allPoints.max()
//        val scoreDelta = max(maxPoints - minPoints, 20)
//        val boarderInset = 0.1f * scoreDelta
//
//        val bounds = RectF(
//            0f,
//            maxPoints + boarderInset,
//            max(2f, pointsHistory.first().size.toFloat() - 1),
//            minPoints - boarderInset,
//        )
//
//        val palette = listOf(
//            MaterialColors.getColor(binding.graphView, R.attr.colorPrimary),
//            MaterialColors.getColor(binding.graphView, R.attr.colorTertiary),
//            MaterialColors.getColor(binding.graphView, R.attr.colorErrorContainer),
//            MaterialColors.getColor(binding.graphView, R.attr.colorOnSurface)
//        )
//
//        binding.graphView.scorePlot = Plot(bounds).apply {
//            lineGraphs.clear()
//
//            for (k in game.players.indices)
//            {
//
//                val plotData = LineGraphData(
//                    points = pointsHistory[k].mapIndexed { index, value ->
//                        PointF(
//                            index.toFloat(),
//                            value.toFloat()
//                        )
//                    },
//                    color = palette[k],
//                    label = game.players[k].name
//                )
//
//                lineGraphs.add(plotData)
//            }
//        }
    }

    private fun findNavController() =
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
}