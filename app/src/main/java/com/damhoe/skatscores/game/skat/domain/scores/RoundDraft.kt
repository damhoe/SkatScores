package com.damhoe.skatscores.game.skat.domain.scores

import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.GrandOrSuit.GrandOrSuitOptions
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.Null.NullOptions
import java.util.UUID

/** What was played. Null is a game of its own, not one of the [SkatSuit] values. */
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

    /** Declarer bid higher than the game was worth. Not available for a Null game. */
    OVERBID,
}

/**
 * A round while it is being entered or edited.
 *
 * This is the single editable representation behind the round sheet: it holds every field of
 * every score type, converts to the matching [SkatScore], and can be rebuilt from one so an
 * existing round can be reopened without losing information.
 */
data class RoundDraft(
    /** Null while entering a new round. */
    val scoreId: UUID? = null,
    /** Null means nobody played: the round was passed. */
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

    /** A Null game cannot be recorded as overbid, so the option is not offered for it. */
    val canBeOverbid: Boolean get() = game != RoundGame.NULL

    /** Spitzen and the win levels have no effect on a Null game. */
    val usesMultipliers: Boolean get() = game != RoundGame.NULL

    /**
     * The points this round would score for its declarer. Drives the live preview in the
     * sheet, so it goes through the same [SkatScore.toPoints] the score board uses.
     */
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

    /**
     * Collapses the toggles onto the flat combination enum. Schwarz implies Schneider there,
     * which is why the Schneider cases come after the Schwarz ones.
     */
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
        /** Stand-in id while a new round has no identity yet; never persisted. */
        private val PREVIEW_ID: UUID = UUID(0L, 0L)

        /** A blank round with the next dealer's left-hand neighbour pre-selected. */
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

        // Schwarz implies Schneider, so the schwarz combinations count as schneider too.
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
