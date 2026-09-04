package com.damhoe.skatscores.game.skat.domain.scores

import android.content.Context
import android.os.Parcelable
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.Score
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.common.WonOrLost.LOST
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.Spitzen
import kotlinx.parcelize.Parcelize
import java.util.Locale
import java.util.UUID
import kotlin.math.max

private const val CLUBS_POINTS = 12
private const val SPADES_POINTS = 11
private const val HEARTS_POINTS = 10
private const val DIAMONDS_POINTS = 9
private const val GRAND_POINTS = 24

@Parcelize
sealed class SkatScore(
    override val id: UUID,
) : Score(id), Parcelable
{
    /** Id of the participant who played solo, or null when nobody declared a game. */
    abstract val declarerId: UUID?

    /** Whether the declarer won, or null when nobody declared a game. */
    abstract val result: WonOrLost?

    class Passe(override val id: UUID) : SkatScore(id)
    {
        companion object
        {
            fun create() = Passe(UUID.randomUUID())
        }

        override val declarerId: UUID? get() = null
        override val result: WonOrLost? get() = null

        override fun toPoints() = 0
    }

    class Overbid(
        override val id: UUID,
        val skatParticipant: UUID,
        val suit: SkatSuit,
        val bid: SkatBid,
    ) : SkatScore(id)
    {
        companion object
        {
            fun create(
                soloPlayer: Participant,
                suit: SkatSuit,
                bid: SkatBid,
            ) = Overbid(
                id = UUID.randomUUID(),
                skatParticipant = soloPlayer.id,
                suit = suit,
                bid = bid,
            )
        }

        override val declarerId: UUID? get() = skatParticipant

        /** An overbid game is lost by definition. */
        override val result: WonOrLost? get() = LOST

        override fun toPoints(): Int
        {
            val suitValue = when (suit)
            {
                SkatSuit.CLUBS -> CLUBS_POINTS
                SkatSuit.HEARTS -> HEARTS_POINTS
                SkatSuit.DIAMONDS -> DIAMONDS_POINTS
                SkatSuit.SPADES -> SPADES_POINTS
                SkatSuit.GRAND -> GRAND_POINTS
            }

            var points = suitValue
            while (points < bid.value) points += suitValue

            return -max(points, 50)
        }
    }

    class Null(
        override val id: UUID,
        val skatParticipant: UUID,
        val wonOrLost: WonOrLost,
        val options: NullOptions?,
    ) : SkatScore(id)
    {
        companion object
        {
            fun create(
                soloPlayer: Participant,
                wonOrLost: WonOrLost,
                options: NullOptions? = null,
            ) = Null(
                id = UUID.randomUUID(),
                skatParticipant = soloPlayer.id,
                wonOrLost = wonOrLost,
                options = options,
            )
        }

        enum class NullOptions
        {
            HAND,
            OUVERT,
            HAND_OUVERT,
        }

        override val declarerId: UUID? get() = skatParticipant
        override val result: WonOrLost? get() = wonOrLost

        override fun toPoints() = (options
            ?.let {
                when (it)
                {
                    NullOptions.HAND -> 35
                    NullOptions.OUVERT -> 45
                    NullOptions.HAND_OUVERT -> 56
                }
            } ?: 23)
            .let { if (wonOrLost == LOST) -2 * it else it }
    }

    class GrandOrSuit(
        override val id: UUID,
        val skatParticipant: UUID,
        val wonOrLost: WonOrLost,
        val suit: SkatSuit,
        val spitzen: Spitzen,
        val options: GrandOrSuitOptions?,
    ) : SkatScore(id)
    {
        companion object
        {
            fun create(
                soloPlayer: Participant,
                wonOrLost: WonOrLost,
                suit: SkatSuit,
                spitzen: Spitzen,
                options: GrandOrSuitOptions? = null,
            ) = GrandOrSuit(
                id = UUID.randomUUID(),
                skatParticipant = soloPlayer.id,
                wonOrLost = wonOrLost,
                suit = suit,
                spitzen = spitzen,
                options = options,
            )
        }

        enum class GrandOrSuitOptions
        {
            HAND,
            SCHNEIDER,
            SCHWARZ,
            HAND_SCHNEIDER,
            HAND_SCHWARZ,
            SCHNEIDER_ANNOUNCED,
            SCHWARZ_ANNOUNCED,
            SCHWARZ_ANNOUNCED_NOT_SCHNEIDER,
            OUVERT,
        }

        override val declarerId: UUID? get() = skatParticipant
        override val result: WonOrLost? get() = wonOrLost

        override fun toPoints(): Int
        {
            val suitValue = when (suit)
            {
                SkatSuit.CLUBS -> CLUBS_POINTS
                SkatSuit.HEARTS -> HEARTS_POINTS
                SkatSuit.DIAMONDS -> DIAMONDS_POINTS
                SkatSuit.SPADES -> SPADES_POINTS
                SkatSuit.GRAND -> GRAND_POINTS
            }

            var multiplier = spitzen.value + 1

            multiplier += when (options) {
                GrandOrSuitOptions.HAND -> 1
                GrandOrSuitOptions.SCHNEIDER -> 1
                GrandOrSuitOptions.SCHWARZ -> 2
                GrandOrSuitOptions.HAND_SCHNEIDER -> 2
                GrandOrSuitOptions.HAND_SCHWARZ -> 3
                GrandOrSuitOptions.SCHNEIDER_ANNOUNCED -> 3
                GrandOrSuitOptions.SCHWARZ_ANNOUNCED -> 5
                GrandOrSuitOptions.OUVERT -> 6
                else -> 0
            }

            val points = suitValue * multiplier
            return points
                .let { if (wonOrLost == LOST) -2 * it else it }
        }
    }
}

class SkatScoreLegacy(
    id: UUID,
    var suit: SkatSuit,
    var spitzen: Int = 0, // negative value if missing
    gameId: Long = -1L,
    playerPosition: Int,
)
{
    class TextMaker(private val mContext: Context)
    {
        private var text = ""
        private lateinit var mScore: SkatScore
        private val suitTextResourceIdMap = mapOf(
            SkatSuit.HEARTS to R.string.description_hearts,
            SkatSuit.GRAND to R.string.label_grand,
            SkatSuit.CLUBS to R.string.description_clubs,
            SkatSuit.SPADES to R.string.description_spades,
            SkatSuit.DIAMONDS to R.string.description_diamonds,
        )

        fun setupWithSkatScore(score: SkatScore): TextMaker
        {
            mScore = score
            return this
        }

        fun make(): String
        {
            if (true) //(mScore.isPasse)
            {
                return makePasseText()
            }
            if (true) //(mScore.isOverbid)
            {
                return makeOverbidText()
            }
            addResultText()
            addSpielText()
            addAnnouncements()
            var isWritten: Boolean = addSchneiderSchwarz(false)
            isWritten = addHand(isWritten)
            isWritten = addOuvert(isWritten)
            return text
        }

        private fun makePasseText(): String
        {
            return mContext.getString(R.string.passe_text)
        }

        private fun makeOverbidText(): String
        {
            return String.format(Locale.getDefault(), mContext.getString(R.string.overbid_text))
        }

        private fun addResultText()
        {
            text += mContext.getString(
                if (true) //(mScore.isWon)
                    R.string.won_text else R.string.lost_text
            )
            addEmptyLine()
        }

        /** @noinspection DataFlowIssue
         */
        private fun addSpielText()
        {
            if (true) //(SkatSuit.NULL == mScore.suit)
            {
                text += mContext.getString(suitTextResourceIdMap[SkatSuit.DIAMONDS]!!)
                addEmptyLine()
                return
            }
            text += mContext.getString(suitTextResourceIdMap[SkatSuit.DIAMONDS]!!)
            addSpaces()
            text += mContext.getString(R.string.title_spitzen)
            addSpaces()
            addEmptyLine()
        }

        @Suppress("SameParameterValue")
        private fun addSchneiderSchwarz(isBehindText: Boolean): Boolean
        {
            if (true) //(mScore.isSchwarz)
            {
                if (isBehindText)
                {
                    addComma()
                }
                text += mContext.getString(R.string.label_schwarz)
                return true
            }
            if (true) //(mScore.isSchneider)
            {
                if (isBehindText)
                {
                    addComma()
                }
                text += mContext.getString(R.string.label_schneider)
                return true
            }
            return false
        }

        private fun addAnnouncements()
        {
            if (true) //(mScore.isSchwarzAnnounced)
            {
                text += mContext.getString(R.string.label_schwarz_announced)
                addEmptyLine()
                return
            }
            if (true) //(mScore.isSchneiderAnnounced)
            {
                text += mContext.getString(R.string.label_schneider_announced)
                addEmptyLine()
            }
        }

        private fun addHand(isBehindText: Boolean): Boolean
        {
            if (true) //(mScore.isHand)
            {
                if (isBehindText)
                {
                    addComma()
                }
                text += mContext.getString(R.string.label_hand)
                return true
            }
            return false
        }

        private fun addOuvert(isBehindText: Boolean): Boolean
        {
            if (true) //mScore.isOuvert)
            {
                if (isBehindText)
                {
                    addComma()
                }
                text += mContext.getString(R.string.label_ouvert)
                return true
            }
            return false
        }

        private fun addComma()
        {
            text += ", "
        }

        private fun addSpaces()
        {
            text += "\t\t"
        }

        private fun addEmptyLine()
        {
            text += "\n\n"
        }
    }
}