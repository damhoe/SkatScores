package com.damhoe.skatscores.game.skat.adapter.presentation

import android.widget.TextView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.databinding.ViewPointsBreakdownBinding
import com.damhoe.skatscores.game.skat.domain.SeatBreakdown

/**
 * Binding for the tournament breakdown, shared by the score board and the chart screen.
 *
 * Both surfaces show the same parts in the same seat order, so they format them here rather
 * than each in their own fragment.
 */

/** How every signed number on the redesigned screens reads. */
fun signed(value: Int) = if (value > 0) "+$value" else value.toString()

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

/** Won - lost as declarer, then games won as defender. */
fun TextView.bindRecord(breakdown: SeatBreakdown?, name: String?)
{
    if (breakdown == null)
    {
        text = ""
        contentDescription = null
        return
    }

    text = context.getString(
        R.string.format_scoreboard_record,
        breakdown.declarerWins,
        breakdown.declarerLosses,
        breakdown.defenderWins,
    )
    // The line is three bare numbers, so spell it out rather than let it be read as one.
    contentDescription = context.getString(
        R.string.description_scoreboard_record,
        name.orEmpty(),
        breakdown.declarerWins,
        breakdown.declarerLosses,
        breakdown.defenderWins,
    )
}
