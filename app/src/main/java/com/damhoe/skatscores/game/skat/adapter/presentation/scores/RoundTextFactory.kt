package com.damhoe.skatscores.game.skat.adapter.presentation.scores

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.scores.RoundDraft
import com.damhoe.skatscores.game.skat.domain.scores.RoundGame
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit

/** E.g. "Clubs · Hand" over "Marlon · Won · 3×"; shared by the round log, sheet and sharing. */
class RoundTextFactory(private val context: Context)
{
    fun titleOf(score: SkatScore): String
    {
        if (score is SkatScore.Passe)
        {
            return context.getString(R.string.label_passe)
        }

        val draft = RoundDraft.fromScore(score, participantsUnused)
        val parts = mutableListOf(gameLabel(draft.game))
        parts += modifierLabels(draft)

        return parts.joinToString(SEPARATOR)
    }

    fun subtitleOf(score: SkatScore, participants: SkatParticipants): String
    {
        val declarerName = score.declarerId
            ?.let { id -> participants.asList().firstOrNull { it.id == id }?.displayName }
            ?: return NO_DETAIL

        val parts = mutableListOf(declarerName, resultLabel(score))

        if (score is SkatScore.GrandOrSuit)
        {
            parts += context.getString(R.string.format_spitzen, score.spitzen.value)
        }

        return parts.joinToString(SEPARATOR)
    }

    fun gameLabel(game: RoundGame): String = context.getString(
        when (game)
        {
            RoundGame.DIAMONDS -> R.string.description_diamonds
            RoundGame.HEARTS -> R.string.description_hearts
            RoundGame.SPADES -> R.string.description_spades
            RoundGame.CLUBS -> R.string.description_clubs
            RoundGame.GRAND -> R.string.label_grand
            RoundGame.NULL -> R.string.label_null
        }
    )

    private fun resultLabel(score: SkatScore) = context.getString(
        when
        {
            score is SkatScore.Overbid -> R.string.label_overbid
            score.result == WonOrLost.WON -> R.string.label_won
            else -> R.string.label_lost
        }
    )

    /** In the order the sheet shows them. */
    private fun modifierLabels(draft: RoundDraft): List<String> = buildList {
        if (draft.hand) add(context.getString(R.string.label_hand))
        if (draft.announced && draft.schwarz) add(context.getString(R.string.label_schwarz_announced))
        else if (draft.announced && draft.schneider) add(context.getString(R.string.label_schneider_announced))
        else if (draft.schwarz) add(context.getString(R.string.label_schwarz))
        else if (draft.schneider) add(context.getString(R.string.label_schneider))
        if (draft.ouvert) add(context.getString(R.string.label_ouvert))
    }

    companion object
    {
        private const val SEPARATOR = " · "

        private const val NO_DETAIL = "–"

        /** Only used to resolve the declarer, which the title ignores. */
        private val participantsUnused = SkatParticipants.createNew()

        fun suitOf(score: SkatScore): SkatSuit? = when (score)
        {
            is SkatScore.GrandOrSuit -> score.suit
            is SkatScore.Overbid -> score.suit
            else -> null
        }

        @DrawableRes
        fun suitIconOf(suit: SkatSuit): Int? = when (suit)
        {
            SkatSuit.CLUBS -> R.drawable.ic_suit_clubs
            SkatSuit.SPADES -> R.drawable.ic_suit_spades
            SkatSuit.HEARTS -> R.drawable.ic_suit_hearts
            SkatSuit.DIAMONDS -> R.drawable.ic_suit_diamonds
            SkatSuit.GRAND -> null
        }

        @AttrRes
        fun suitColorAttrOf(suit: SkatSuit): Int = when (suit)
        {
            SkatSuit.CLUBS -> R.attr.colorClubs
            SkatSuit.SPADES -> R.attr.colorSpades
            SkatSuit.HEARTS -> R.attr.colorHearts
            SkatSuit.DIAMONDS -> R.attr.colorDiamonds
            SkatSuit.GRAND -> R.attr.colorOnSurfaceVariant
        }
    }
}
