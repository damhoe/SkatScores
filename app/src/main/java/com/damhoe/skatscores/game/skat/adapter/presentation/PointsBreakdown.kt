package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.shared.signed
import com.damhoe.skatscores.databinding.ViewPointsBreakdownBinding
import com.damhoe.skatscores.game.skat.domain.SeatBreakdown

/**
 * Binding for the tournament breakdown, shared by the score board and the chart screen.
 *
 * Both surfaces show the same parts in the same seat order, so they format them here rather
 * than each in their own fragment.
 */

/** Fills the three labelled rows of view_points_breakdown, one column per seat. */
fun ViewPointsBreakdownBinding.bindBreakdown(breakdowns: List<SeatBreakdown>)
{
    listOf(gameValue1, gameValue2, gameValue3).forEachIndexed { seat, view ->
        view.text = signed(breakdowns.getOrNull(seat)?.gameValue ?: 0)
    }
    listOf(soloBonus1, soloBonus2, soloBonus3).forEachIndexed { seat, view ->
        view.text = signed(breakdowns.getOrNull(seat)?.soloBonus ?: 0)
    }
    listOf(againstBonus1, againstBonus2, againstBonus3).forEachIndexed { seat, view ->
        view.text = signed(breakdowns.getOrNull(seat)?.againstBonus ?: 0)
    }
}
