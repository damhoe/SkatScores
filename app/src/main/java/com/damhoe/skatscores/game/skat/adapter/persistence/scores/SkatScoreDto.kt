package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGameDto
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.skat.domain.SkatPlayerCount
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.GrandOrSuit
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore.Null
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import java.time.Instant
import java.util.UUID

data class SkatScoreDto(
    val id: UUID,
    val gameId: UUID,
    val skatParticipantId: UUID? = null,
    /** See [RoundSequence]. */
    val seq: String,
    val type: SkatScoreType,

    val wonOrLost: WonOrLost? = null,

    val overbidSuit: SkatSuit? = null,
    val overbidBidValue: Int? = null,

    val nullOptions: Null.NullOptions? = null,

    val grandOrSuitSuit: SkatSuit? = null,
    val grandOrSuitSpitzenValue: Int? = null,
    val grandOrSuitOptions: GrandOrSuit.GrandOrSuitOptions? = null
)
{
    fun toContentValues() = contentValuesOf(
        SkatScoresTable.COLUMN_ID to id.toString(),
        SkatScoresTable.COLUMN_GAME_ID to gameId.toString(),
        SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID to skatParticipantId?.toString(),
        SkatScoresTable.COLUMN_SCORE_TYPE to type.name,
        SkatScoresTable.COLUMN_SEQ to seq,
        SkatScoresTable.COLUMN_WON_OR_LOST to wonOrLost?.name,
        SkatScoresTable.COLUMN_OVERBID_SUIT to overbidSuit?.name,
        SkatScoresTable.COLUMN_OVERBID_BID_VALUE to overbidBidValue,
        SkatScoresTable.COLUMN_NULL_OPTIONS to nullOptions?.name,
        SkatScoresTable.COLUMN_GRAND_OR_SUIT_SUIT to grandOrSuitSuit?.name,
        SkatScoresTable.COLUMN_GRAND_OR_SUIT_SPITZEN_VALUE to grandOrSuitSpitzenValue,
        SkatScoresTable.COLUMN_GRAND_OR_SUIT_OPTIONS to grandOrSuitOptions?.name,
    )

    fun toEditContentValues() = toContentValues().apply {
        remove(SkatScoresTable.COLUMN_ID)
        remove(SkatScoresTable.COLUMN_GAME_ID)
        remove(SkatScoresTable.COLUMN_SEQ)
    }

    fun toSkatScore(): SkatScore
    {
        return when (type)
        {
            SkatScoreType.PASSE -> SkatScore.Passe(id = id)
            SkatScoreType.OVERBID ->
            {
                SkatScore.Overbid(
                    id = id,
                    skatParticipant = skatParticipantId!!,
                    suit = overbidSuit!!,
                    bid = SkatBid(overbidBidValue!!)
                )
            }

            SkatScoreType.NULL -> Null(
                id = id,
                skatParticipant = skatParticipantId!!,
                wonOrLost = wonOrLost!!,
                options = nullOptions
            )

            SkatScoreType.GRAND_OR_SUIT -> GrandOrSuit(
                id = id,
                skatParticipant = skatParticipantId!!,
                wonOrLost = wonOrLost!!,
                suit = grandOrSuitSuit!!,
                spitzen = Spitzen(grandOrSuitSpitzenValue!!),
                options = grandOrSuitOptions
            )
        }
    }

    companion object
    {
        fun mapFrom(
            skatScore: SkatScore,
            gameId: UUID,
            seq: String,
        ): SkatScoreDto
        {
            return when (skatScore)
            {
                is SkatScore.Passe -> SkatScoreDto(
                    id = skatScore.id,
                    gameId = gameId,
                    type = SkatScoreType.PASSE,
                    seq = seq,
                )

                is SkatScore.Overbid -> SkatScoreDto(
                    id = skatScore.id,
                    gameId = gameId,
                    type = SkatScoreType.OVERBID,
                    seq = seq,
                    skatParticipantId = skatScore.skatParticipant,
                    overbidSuit = skatScore.suit,
                    overbidBidValue = skatScore.bid.value
                )

                is Null -> SkatScoreDto(
                    id = skatScore.id,
                    gameId = gameId,
                    type = SkatScoreType.NULL,
                    seq = seq,
                    skatParticipantId = skatScore.skatParticipant,
                    wonOrLost = skatScore.wonOrLost,
                    nullOptions = skatScore.options
                )

                is GrandOrSuit -> SkatScoreDto(
                    id = skatScore.id,
                    gameId = gameId,
                    type = SkatScoreType.GRAND_OR_SUIT,
                    seq = seq,
                    skatParticipantId = skatScore.skatParticipant,
                    wonOrLost = skatScore.wonOrLost,
                    grandOrSuitSuit = skatScore.suit,
                    grandOrSuitSpitzenValue = skatScore.spitzen.value,
                    grandOrSuitOptions = skatScore.options
                )
            }
        }

        fun mapFrom(cursor: Cursor): SkatScoreDto
        {
            val id = cursor.getUuidOrNull(SkatScoresTable.COLUMN_ID)!!
            val gameId = cursor.getUuidOrNull(SkatScoresTable.COLUMN_GAME_ID)!!
            val skatParticipantId =
                cursor.getUuidOrNull(SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID)
            val seq = cursor.getStringOrNull(SkatScoresTable.COLUMN_SEQ)!!
            val typeString = cursor.getStringOrNull(SkatScoresTable.COLUMN_SCORE_TYPE)!!
            val type = SkatScoreType.valueOf(typeString)
            val wonOrLost = cursor.getStringOrNull(SkatScoresTable.COLUMN_WON_OR_LOST)
                ?.let { WonOrLost.valueOf(it) }
            val overbidSuit = cursor.getStringOrNull(SkatScoresTable.COLUMN_OVERBID_SUIT)
                ?.let { SkatSuit.valueOf(it) }
            val overbidBidValue = cursor.getIntOrNull(SkatScoresTable.COLUMN_OVERBID_BID_VALUE)
            val nullOptions = cursor.getStringOrNull(SkatScoresTable.COLUMN_NULL_OPTIONS)
                ?.let { Null.NullOptions.valueOf(it) }
            val grandOrSuitSuit = cursor.getStringOrNull(SkatScoresTable.COLUMN_GRAND_OR_SUIT_SUIT)
                ?.let { SkatSuit.valueOf(it) }
            val grandOrSuitSpitzenValue =
                cursor.getIntOrNull(SkatScoresTable.COLUMN_GRAND_OR_SUIT_SPITZEN_VALUE)
            val grandOrSuitOptions =
                cursor.getStringOrNull(SkatScoresTable.COLUMN_GRAND_OR_SUIT_OPTIONS)
                    ?.let { GrandOrSuit.GrandOrSuitOptions.valueOf(it) }

            return SkatScoreDto(
                id = id,
                gameId = gameId,
                type = type,
                skatParticipantId = skatParticipantId,
                seq = seq,
                wonOrLost = wonOrLost,
                overbidSuit = overbidSuit,
                overbidBidValue = overbidBidValue,
                nullOptions = nullOptions,
                grandOrSuitSuit = grandOrSuitSuit,
                grandOrSuitSpitzenValue = grandOrSuitSpitzenValue,
                grandOrSuitOptions = grandOrSuitOptions
            )
        }
    }
}

enum class SkatScoreType
{
    PASSE,
    OVERBID,
    NULL,
    GRAND_OR_SUIT,
}