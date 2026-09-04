package com.damhoe.skatscores.game.skat.adapter.presentation

import android.content.Context
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.SharedList
import com.damhoe.skatscores.game.common.SharedRound
import com.damhoe.skatscores.game.common.SharedStanding
import com.damhoe.skatscores.game.skat.adapter.presentation.scores.RoundTextFactory
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.shared.asListDate
import com.damhoe.skatscores.shared.signed

/**
 * A Skat list as text to share. The rounds read the way the round log does - same labels from
 * the same [RoundTextFactory] - so a shared list and the screen it came from cannot say
 * different things about the same round.
 */
fun SkatGame.asSharedList(context: Context, roundText: RoundTextFactory): SharedList
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
            sharedRound(number = index + 1, score = score, roundText = roundText)
        },
        footer = context.getString(R.string.format_share_footer, context.getString(R.string.app_name)),
    )
}

/** A passed round has no declarer and is worth nothing, so it is named and left at that. */
private fun SkatGame.sharedRound(
    number: Int,
    score: SkatScore,
    roundText: RoundTextFactory,
): SharedRound
{
    val headline = roundText.titleOf(score)

    if (score is SkatScore.Passe) return SharedRound(number, headline)

    return SharedRound(
        number = number,
        headline = headline,
        detail = roundText.subtitleOf(score, participants),
        value = signed(score.toPoints()),
    )
}
