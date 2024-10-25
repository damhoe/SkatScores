package com.damhoe.skatscores.game.domain.score

import android.content.Context
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.domain.skat.application.Constant
import com.damhoe.skatscores.game.domain.skat.application.PointsCalculator
import java.util.Locale

class SkatScore(
    var suit: SkatSuit = SkatSuit.NONE, // Clubs, Diamonds, Hears, Spades, Null, Grand, Invalid
    var outcome: SkatOutcome = SkatOutcome.PASSE, // Won, Lost, Overbid, Passe
    var spitzen: Int = 0, // negative value if missing
    var isHand: Boolean = false,
    var isSchneider: Boolean = false,
    var isSchneiderAnnounced: Boolean = false,
    var isSchwarz: Boolean = false,
    var isSchwarzAnnounced: Boolean = false,
    var isOuvert: Boolean = false,
    gameId: Long = -1L,
    playerPosition: Int = Constant.PASSE_PLAYER_POSITION,
) : Score(gameId = gameId, playerPosition = playerPosition) {

    override var id = super.id

    constructor(command: SkatScoreCommand) : this(
        spitzen = command.spitzen,
        suit = command.suit,
        isHand = command.isHand,
        isSchneider = command.isSchneider,
        isSchwarz = command.isSchwarz,
        isOuvert = command.isOuvert,
        outcome = command.outcome,
        isSchneiderAnnounced = command.isSchneiderAnnounced,
        isSchwarzAnnounced = command.isSchwarzAnnounced,
        gameId = command.gameId,
        playerPosition = command.playerPosition,
    )

    override fun toPoints(): Int = PointsCalculator(this).calculatePoints()

    val isWon: Boolean
        get() = SkatOutcome.WON == outcome
    val isPasse: Boolean
        get() = playerPosition == Constant.PASSE_PLAYER_POSITION
    val isOverbid: Boolean
        get() = SkatOutcome.OVERBID == outcome

    class TextMaker(private val mContext: Context) {
        private var text = ""
        private lateinit var mScore: SkatScore
        private val suitTextResourceIdMap = mapOf(
            SkatSuit.NULL to R.string.label_null,
            SkatSuit.HEARTS to R.string.description_hearts,
            SkatSuit.GRAND to R.string.label_grand,
            SkatSuit.CLUBS to R.string.description_clubs,
            SkatSuit.SPADES to R.string.description_spades,
            SkatSuit.DIAMONDS to R.string.description_diamonds,
            SkatSuit.NONE to R.string.unknown
        )

        fun setupWithSkatScore(score: SkatScore): TextMaker
        {
            mScore = score
            return this
        }

        fun make(): String {
            if (mScore.isPasse) {
                return makePasseText()
            }
            if (mScore.isOverbid) {
                return makeOverbidText()
            }
            addResultText()
            addSpielText()
            addAnnouncements()
            var isWritten: Boolean
            isWritten = addSchneiderSchwarz(false)
            isWritten = addHand(isWritten)
            isWritten = addOuvert(isWritten)
            return text
        }

        private fun makePasseText(): String {
            return mContext.getString(R.string.passe_text)
        }

        private fun makeOverbidText(): String {
            return String.format(Locale.getDefault(), mContext.getString(R.string.overbid_text))
        }

        private fun addResultText() {
            text += mContext.getString(if (mScore.isWon) R.string.won_text else R.string.lost_text)
            addEmptyLine()
        }

        /** @noinspection DataFlowIssue
         */
        private fun addSpielText() {
            if (SkatSuit.NULL == mScore.suit) {
                text += mContext.getString(suitTextResourceIdMap[mScore.suit]!!)
                addEmptyLine()
                return
            }
            text += mContext.getString(suitTextResourceIdMap[mScore.suit]!!)
            addSpaces()
            text += mContext.getString(R.string.title_spitzen)
            addSpaces()
            text += mScore.spitzen.toString()
            addEmptyLine()
        }

        @Suppress("SameParameterValue")
        private fun addSchneiderSchwarz(isBehindText: Boolean): Boolean {
            if (mScore.isSchwarz) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_schwarz)
                return true
            }
            if (mScore.isSchneider) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_schneider)
                return true
            }
            return false
        }

        private fun addAnnouncements() {
            if (mScore.isSchwarzAnnounced) {
                text += mContext.getString(R.string.label_schwarz_announced)
                addEmptyLine()
                return
            }
            if (mScore.isSchneiderAnnounced) {
                text += mContext.getString(R.string.label_schneider_announced)
                addEmptyLine()
            }
        }

        private fun addHand(isBehindText: Boolean): Boolean {
            if (mScore.isHand) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_hand)
                return true
            }
            return false
        }

        private fun addOuvert(isBehindText: Boolean): Boolean {
            if (mScore.isOuvert) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_ouvert)
                return true
            }
            return false
        }

        private fun addComma() {
            text += ", "
        }

        private fun addSpaces() {
            text += "\t\t"
        }

        private fun addEmptyLine() {
            text += "\n\n"
        }
    }
}