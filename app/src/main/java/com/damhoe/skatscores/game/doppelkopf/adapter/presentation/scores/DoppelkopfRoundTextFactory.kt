package com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores

import android.content.Context
import androidx.annotation.StringRes
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore

/**
 * Turns a Doppelkopf round into the lines shown in the round log, e.g. "Solo · Damen" over
 * "Marlon · keine 60 · Re". Both the log and the round sheet read their labels from here so the
 * wording cannot drift apart.
 */
class DoppelkopfRoundTextFactory(private val context: Context)
{
    /** What was played: a normal round, or which solo. */
    fun titleOf(score: DoppelkopfScore): String = when (score)
    {
        is DoppelkopfScore.Normal -> context.getString(R.string.label_round_normal)
        is DoppelkopfScore.Solo -> context.getString(
            R.string.format_solo_round,
            context.getString(soloKindLabel(score.soloKind)),
        )
    }

    /** Who was Re, how clearly it went, and what was announced. */
    fun subtitleOf(score: DoppelkopfScore, participants: DoppelkopfParticipants): String
    {
        val parts = mutableListOf(reNames(score, participants))

        if (score.value.winLevel != DoppelkopfWinLevel.WON)
        {
            parts += context.getString(winLevelLabel(score.value.winLevel))
        }

        announcementLabel(score)?.let { parts += it }

        return parts.filter { it.isNotEmpty() }.joinToString(SEPARATOR)
    }

    /** The seats that played as Re, which is the soloist on their own in a solo. */
    private fun reNames(
        score: DoppelkopfScore,
        participants: DoppelkopfParticipants,
    ): String = participants.asList()
        .filter { score.partyOf(it.id) == DoppelkopfParty.RE }
        .joinToString(", ") { it.displayName }

    /** Re, Kontra, or both - whichever was called before the round was played. */
    private fun announcementLabel(score: DoppelkopfScore): String?
    {
        val announcements = buildList {
            if (score.value.reAnnounced) add(context.getString(R.string.label_re))
            if (score.value.kontraAnnounced) add(context.getString(R.string.label_kontra))
        }

        return announcements.takeIf { it.isNotEmpty() }?.joinToString(" + ")
    }

    fun partyLabel(party: DoppelkopfParty): String = context.getString(
        when (party)
        {
            DoppelkopfParty.RE -> R.string.label_re
            DoppelkopfParty.KONTRA -> R.string.label_kontra
        }
    )

    companion object
    {
        private const val SEPARATOR = " · "

        @StringRes
        fun soloKindLabel(kind: DoppelkopfSoloKind): Int = when (kind)
        {
            DoppelkopfSoloKind.SUIT -> R.string.label_solo_suit
            DoppelkopfSoloKind.QUEENS -> R.string.label_solo_queens
            DoppelkopfSoloKind.JACKS -> R.string.label_solo_jacks
            DoppelkopfSoloKind.ACES -> R.string.label_solo_aces
            DoppelkopfSoloKind.FLESHLESS -> R.string.label_solo_fleshless
        }

        @StringRes
        fun winLevelLabel(level: DoppelkopfWinLevel): Int = when (level)
        {
            DoppelkopfWinLevel.WON -> R.string.label_win_level_won
            DoppelkopfWinLevel.NO_90 -> R.string.label_win_level_no_90
            DoppelkopfWinLevel.NO_60 -> R.string.label_win_level_no_60
            DoppelkopfWinLevel.NO_30 -> R.string.label_win_level_no_30
            DoppelkopfWinLevel.SCHWARZ -> R.string.label_win_level_schwarz
        }
    }
}
