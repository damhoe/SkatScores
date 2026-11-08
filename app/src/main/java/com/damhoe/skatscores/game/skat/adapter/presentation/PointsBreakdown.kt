package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.shared.signed
import com.damhoe.skatscores.databinding.ViewPointsBreakdownBinding
import com.damhoe.skatscores.game.skat.domain.SeatBreakdown

/** Shared by the score board and the chart screen. */

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
