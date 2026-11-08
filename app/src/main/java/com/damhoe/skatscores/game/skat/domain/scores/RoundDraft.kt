package com.damhoe.skatscores.game.skat.domain.scores

import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.GrandOrSuit.GrandOrSuitOptions
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.Null.NullOptions
import java.util.UUID

/** Null is a game of its own, not a [SkatSuit]. */
enum class RoundGame
{
    DIAMONDS,
    HEARTS,
    SPADES,
    CLUBS,
    GRAND,
    NULL,
    ;

    val suit: SkatSuit?
        get() = when (this)
        {
            DIAMONDS -> SkatSuit.DIAMONDS
            HEARTS -> SkatSuit.HEARTS
            SPADES -> SkatSuit.SPADES
            CLUBS -> SkatSuit.CLUBS
            GRAND -> SkatSuit.GRAND
            NULL -> null
        }

    /** Only the four jacks are trump in a Grand. */
    val maxSpitzen: Int
        get() = if (this == GRAND) 4 else 11

    companion object
    {
        fun of(suit: SkatSuit) = when (suit)
        {
            SkatSuit.DIAMONDS -> DIAMONDS
            SkatSuit.HEARTS -> HEARTS
            SkatSuit.SPADES -> SPADES
            SkatSuit.CLUBS -> CLUBS
            SkatSuit.GRAND -> GRAND
        }
    }
}

enum class RoundResult
{
    WON,
    LOST,

    OVERBID,
}

/** Editable form of any [SkatScore]; converts both ways without losing information. */
data class RoundDraft(
    val scoreId: UUID? = null,
    /** Null for a passed round. */
    val declarer: Participant? = null,
    val game: RoundGame = RoundGame.CLUBS,
    val spitzen: Spitzen = Spitzen(1),
    val hand: Boolean = false,
    val schneider: Boolean = false,
    val schwarz: Boolean = false,
    val announced: Boolean = false,
    val ouvert: Boolean = false,
    val result: RoundResult = RoundResult.WON,
    val bid: SkatBid = SkatBid(SkatBid.BiddingValues.first()),
)
{
    val isPasse: Boolean get() = declarer == null

    val isEditingExistingRound: Boolean get() = scoreId != null

    val canBeOverbid: Boolean get() = game != RoundGame.NULL

    val usesMultipliers: Boolean get() = game != RoundGame.NULL

    val points: Int get() = toSkatScore(scoreId ?: PREVIEW_ID).toPoints()

    fun toSkatScore(id: UUID = scoreId ?: UUID.randomUUID()): SkatScore
    {
        val declarer = declarer ?: return SkatScore.Passe(id)

        if (game == RoundGame.NULL)
        {
            return SkatScore.Null(
                id = id,
                skatParticipant = declarer.id,
                wonOrLost = wonOrLost(),
                options = nullOptions(),
            )
        }

        val suit = game.suit ?: return SkatScore.Passe(id)

        if (result == RoundResult.OVERBID)
        {
            return SkatScore.Overbid(
                id = id,
                skatParticipant = declarer.id,
                suit = suit,
                bid = bid,
            )
        }

        return SkatScore.GrandOrSuit(
            id = id,
            skatParticipant = declarer.id,
            wonOrLost = wonOrLost(),
            suit = suit,
            spitzen = spitzen,
            options = grandOrSuitOptions(),
        )
    }

    private fun wonOrLost() =
        if (result == RoundResult.WON) WonOrLost.WON else WonOrLost.LOST

    private fun nullOptions() = when
    {
        hand && ouvert -> NullOptions.HAND_OUVERT
        ouvert -> NullOptions.OUVERT
        hand -> NullOptions.HAND
        else -> null
    }

    /** Schwarz implies Schneider, so the Schwarz cases must be checked first. */
    private fun grandOrSuitOptions() = when
    {
        ouvert -> GrandOrSuitOptions.OUVERT
        schwarz && announced -> GrandOrSuitOptions.SCHWARZ_ANNOUNCED
        schwarz -> if (hand) GrandOrSuitOptions.HAND_SCHWARZ else GrandOrSuitOptions.SCHWARZ
        schneider && announced -> GrandOrSuitOptions.SCHNEIDER_ANNOUNCED
        schneider -> if (hand) GrandOrSuitOptions.HAND_SCHNEIDER else GrandOrSuitOptions.SCHNEIDER
        hand -> GrandOrSuitOptions.HAND
        else -> null
    }

    companion object
    {
        /** Only for computing a preview; never persisted. */
        private val PREVIEW_ID: UUID = UUID(0L, 0L)

        fun forNewRound(declarer: Participant?) = RoundDraft(declarer = declarer)

        fun fromScore(score: SkatScore, participants: SkatParticipants): RoundDraft
        {
            val declarer = score.declarerId
                ?.let { id -> participants.asList().firstOrNull { it.id == id } }

            return when (score)
            {
                is SkatScore.Passe -> RoundDraft(scoreId = score.id, declarer = null)

                is SkatScore.Overbid -> RoundDraft(
                    scoreId = score.id,
                    declarer = declarer,
                    game = RoundGame.of(score.suit),
                    result = RoundResult.OVERBID,
                    bid = score.bid,
                )

                is SkatScore.Null -> RoundDraft(
                    scoreId = score.id,
                    declarer = declarer,
                    game = RoundGame.NULL,
                    hand = score.options == NullOptions.HAND ||
                            score.options == NullOptions.HAND_OUVERT,
                    ouvert = score.options == NullOptions.OUVERT ||
                            score.options == NullOptions.HAND_OUVERT,
                    result = resultOf(score.wonOrLost),
                )

                is SkatScore.GrandOrSuit -> RoundDraft(
                    scoreId = score.id,
                    declarer = declarer,
                    game = RoundGame.of(score.suit),
                    spitzen = score.spitzen,
                    hand = score.options in handOptions,
                    schneider = score.options in schneiderOptions,
                    schwarz = score.options in schwarzOptions,
                    announced = score.options in announcedOptions,
                    ouvert = score.options == GrandOrSuitOptions.OUVERT,
                    result = resultOf(score.wonOrLost),
                )
            }
        }

        private fun resultOf(wonOrLost: WonOrLost) =
            if (wonOrLost == WonOrLost.WON) RoundResult.WON else RoundResult.LOST

        private val handOptions = setOf(
            GrandOrSuitOptions.HAND,
            GrandOrSuitOptions.HAND_SCHNEIDER,
            GrandOrSuitOptions.HAND_SCHWARZ,
        )

        private val schneiderOptions = setOf(
            GrandOrSuitOptions.SCHNEIDER,
            GrandOrSuitOptions.HAND_SCHNEIDER,
            GrandOrSuitOptions.SCHNEIDER_ANNOUNCED,
            GrandOrSuitOptions.SCHWARZ,
            GrandOrSuitOptions.HAND_SCHWARZ,
            GrandOrSuitOptions.SCHWARZ_ANNOUNCED,
        )

        private val schwarzOptions = setOf(
            GrandOrSuitOptions.SCHWARZ,
            GrandOrSuitOptions.HAND_SCHWARZ,
            GrandOrSuitOptions.SCHWARZ_ANNOUNCED,
            GrandOrSuitOptions.SCHWARZ_ANNOUNCED_NOT_SCHNEIDER,
        )

        private val announcedOptions = setOf(
            GrandOrSuitOptions.SCHNEIDER_ANNOUNCED,
            GrandOrSuitOptions.SCHWARZ_ANNOUNCED,
            GrandOrSuitOptions.SCHWARZ_ANNOUNCED_NOT_SCHNEIDER,
        )
    }
}
