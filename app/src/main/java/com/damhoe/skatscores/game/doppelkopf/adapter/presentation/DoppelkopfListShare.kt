package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import android.content.Context
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.SharedList
import com.damhoe.skatscores.game.common.SharedRound
import com.damhoe.skatscores.game.common.SharedStanding
import com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores.DoppelkopfRoundTextFactory
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.shared.asListDate
import com.damhoe.skatscores.shared.signed

/**
 * A Doppelkopf list as text to share. Every round is won by one of the two parties, so each
 * line carries the winning side next to what the round was worth, the way the badge does in
 * the round log.
 */
fun DoppelkopfGame.asSharedList(
    context: Context,
    roundText: DoppelkopfRoundTextFactory,
): SharedList
{
    val names = participants.asList().map { it.displayName }
    val totals = calculateTotalPoints()

    return SharedList(
        title = title.value,
        subtitle = context.getString(
            R.string.format_list_meta,
            playedAt.asListDate(),
            context.getString(
                R.string.label_round_progress,
                scores.size,
                settings.roundCount.value,
            ),
        ),
        standings = names.mapIndexed { seat, name ->
            SharedStanding(name, totals.getOrElse(seat) { 0 })
        },
        rounds = scores.mapIndexed { index, score ->
            SharedRound(
                number = index + 1,
                headline = roundText.titleOf(score),
                detail = roundText.subtitleOf(score, participants),
                value = "${roundText.partyLabel(score.winner)} ${signed(valueOf(score))}",
            )
        },
        footer = context.getString(R.string.format_share_footer, context.getString(R.string.app_name)),
    )
}
