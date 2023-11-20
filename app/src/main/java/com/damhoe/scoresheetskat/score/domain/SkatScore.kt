package com.damhoe.scoresheetskat.score.domain

import android.content.Context
import com.damhoe.scoresheetskat.R
import com.damhoe.scoresheetskat.score.Constant
import com.damhoe.scoresheetskat.score.application.PointsCalculator
import java.util.Locale
import java.util.UUID

class SkatScore : Score {
    var spitzen = 0 // negative value if missing
    lateinit var suit: SkatSuit // Clubs, Diamonds, Hears, Spades, Null, Grand
    lateinit var result: SkatResult // Won, Lost, Overbid, Passe
    var isHand = false
    var isSchneider = false
    var isSchneiderAnnounced = false
    var isSchwarz = false
    var isSchwarzAnnounced = false
    var isOuvert = false

    constructor()
    constructor(command: SkatScoreCommand) {
        id = UUID.randomUUID().mostSignificantBits
        spitzen = command.spitzen
        suit = command.suit
        isHand = command.isHand
        isSchneider = command.isSchneider
        isSchwarz = command.isSchwarz
        isOuvert = command.isOuvert
        result = command.result
        isSchneiderAnnounced = command.isSchneiderAnnounced
        isSchwarzAnnounced = command.isSchwarzAnnounced
        gameId = command.gameId
        playerPosition = command.playerPosition
    }

    override fun toPoints(): Int = PointsCalculator(this).calculatePoints()

    val isWon: Boolean
        get() = SkatResult.WON == result
    val isPasse: Boolean
        get() = playerPosition == Constant.PASSE_PLAYER_POSITION
    val isOverbid: Boolean
        get() = SkatResult.OVERBID == result

    class TextMaker(private val mContext: Context) {
        private var text = ""
        private lateinit var mScore: SkatScore
        private val suitTextResourceIdMap: MutableMap<SkatSuit?, Int> = HashMap()

        init {
            suitTextResourceIdMap[SkatSuit.NULL] = R.string.label_null
            suitTextResourceIdMap[SkatSuit.HEARTS] = R.string.description_hearts
            suitTextResourceIdMap[SkatSuit.GRAND] = R.string.label_grand
            suitTextResourceIdMap[SkatSuit.CLUBS] = R.string.description_clubs
            suitTextResourceIdMap[SkatSuit.SPADES] = R.string.description_spades
            suitTextResourceIdMap[SkatSuit.DIAMONDS] = R.string.description_diamonds
            suitTextResourceIdMap[SkatSuit.INVALID] = R.string.unknown
        }

        fun setupWithSkatScore(score: SkatScore): TextMaker {
            mScore = score
            return this
        }

        fun make(): String {
            if (mScore!!.isPasse) {
                return makePasseText()
            }
            if (mScore!!.isOverbid) {
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
            text += mContext.getString(if (mScore!!.isWon) R.string.won_text else R.string.lost_text)
            addEmptyLine()
        }

        /** @noinspection DataFlowIssue
         */
        private fun addSpielText() {
            if (SkatSuit.NULL == mScore!!.suit) {
                text += mContext.getString(suitTextResourceIdMap[mScore!!.suit]!!)
                addEmptyLine()
                return
            }
            text += mContext.getString(suitTextResourceIdMap[mScore!!.suit]!!)
            addSpaces()
            text += mContext.getString(R.string.title_spitzen)
            addSpaces()
            text += mScore!!.spitzen.toString()
            addEmptyLine()
        }

        /** @noinspection SameParameterValue
         */
        private fun addSchneiderSchwarz(isBehindText: Boolean): Boolean {
            if (mScore!!.isSchwarz) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_schwarz)
                return true
            }
            if (mScore!!.isSchneider) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_schneider)
                return true
            }
            return false
        }

        private fun addAnnouncements() {
            if (mScore!!.isSchwarzAnnounced) {
                text += mContext.getString(R.string.label_schwarz_announced)
                addEmptyLine()
                return
            }
            if (mScore!!.isSchneiderAnnounced) {
                text += mContext.getString(R.string.label_schneider_announced)
                addEmptyLine()
            }
        }

        private fun addHand(isBehindText: Boolean): Boolean {
            if (mScore!!.isHand) {
                if (isBehindText) {
                    addComma()
                }
                text += mContext.getString(R.string.label_hand)
                return true
            }
            return false
        }

        private fun addOuvert(isBehindText: Boolean): Boolean {
            if (mScore!!.isOuvert) {
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