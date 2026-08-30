package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSoloKind
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfWinLevel
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfRoundValue
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfScoresTable
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import java.util.UUID

enum class DoppelkopfScoreType
{
    NORMAL,
    SOLO,
}

data class DoppelkopfScoreDto(
    val id: UUID,
    val gameId: UUID,
    val round: Int,
    val type: DoppelkopfScoreType,
    val winner: DoppelkopfParty,

    val reParticipant1: UUID? = null,
    val reParticipant2: UUID? = null,

    val soloistId: UUID? = null,
    val soloKind: DoppelkopfSoloKind? = null,

    val winLevel: DoppelkopfWinLevel,
    val absage: DoppelkopfWinLevel? = null,
    val reAnnounced: Boolean = false,
    val kontraAnnounced: Boolean = false,
    val extraPointsRe: Int = 0,
    val extraPointsKontra: Int = 0,
)
{
    fun toContentValues() = contentValuesOf(
        DoppelkopfScoresTable.COLUMN_ID to id.toString(),
        DoppelkopfScoresTable.COLUMN_GAME_ID to gameId.toString(),
        DoppelkopfScoresTable.COLUMN_ROUND to round,
        DoppelkopfScoresTable.COLUMN_SCORE_TYPE to type.name,
        DoppelkopfScoresTable.COLUMN_WINNER to winner.name,
        DoppelkopfScoresTable.COLUMN_RE_PARTICIPANT_1 to reParticipant1?.toString(),
        DoppelkopfScoresTable.COLUMN_RE_PARTICIPANT_2 to reParticipant2?.toString(),
        DoppelkopfScoresTable.COLUMN_SOLOIST_ID to soloistId?.toString(),
        DoppelkopfScoresTable.COLUMN_SOLO_KIND to soloKind?.name,
        DoppelkopfScoresTable.COLUMN_WIN_LEVEL to winLevel.name,
        DoppelkopfScoresTable.COLUMN_ABSAGE to absage?.name,
        DoppelkopfScoresTable.COLUMN_RE_ANNOUNCED to if (reAnnounced) 1 else 0,
        DoppelkopfScoresTable.COLUMN_KONTRA_ANNOUNCED to if (kontraAnnounced) 1 else 0,
        DoppelkopfScoresTable.COLUMN_EXTRA_POINTS_RE to extraPointsRe,
        DoppelkopfScoresTable.COLUMN_EXTRA_POINTS_KONTRA to extraPointsKontra,
    )

    fun toDoppelkopfScore(): DoppelkopfScore
    {
        val value = DoppelkopfRoundValue(
            winLevel = winLevel,
            absage = absage,
            reAnnounced = reAnnounced,
            kontraAnnounced = kontraAnnounced,
            extraPointsRe = extraPointsRe,
            extraPointsKontra = extraPointsKontra,
        )

        return when (type)
        {
            DoppelkopfScoreType.NORMAL -> DoppelkopfScore.Normal(
                id = id,
                reSeats = setOfNotNull(reParticipant1, reParticipant2),
                winner = winner,
                value = value,
            )

            DoppelkopfScoreType.SOLO -> DoppelkopfScore.Solo(
                id = id,
                soloist = soloistId!!,
                soloKind = soloKind ?: DoppelkopfSoloKind.SUIT,
                winner = winner,
                value = value,
            )
        }
    }

    companion object
    {
        fun mapFrom(
            score: DoppelkopfScore,
            gameId: UUID,
            round: Int,
        ): DoppelkopfScoreDto
        {
            val value = score.value
            val common = DoppelkopfScoreDto(
                id = score.id,
                gameId = gameId,
                round = round,
                type = DoppelkopfScoreType.NORMAL,
                winner = score.winner,
                winLevel = value.winLevel,
                absage = value.absage,
                reAnnounced = value.reAnnounced,
                kontraAnnounced = value.kontraAnnounced,
                extraPointsRe = value.extraPointsRe,
                extraPointsKontra = value.extraPointsKontra,
            )

            return when (score)
            {
                is DoppelkopfScore.Normal ->
                {
                    val reSeats = score.reSeats.toList()
                    common.copy(
                        type = DoppelkopfScoreType.NORMAL,
                        reParticipant1 = reSeats.getOrNull(0),
                        reParticipant2 = reSeats.getOrNull(1),
                    )
                }

                is DoppelkopfScore.Solo -> common.copy(
                    type = DoppelkopfScoreType.SOLO,
                    soloistId = score.soloist,
                    soloKind = score.soloKind,
                )
            }
        }

        fun mapFrom(cursor: Cursor): DoppelkopfScoreDto = DoppelkopfScoreDto(
            id = cursor.getUuidOrNull(DoppelkopfScoresTable.COLUMN_ID)!!,
            gameId = cursor.getUuidOrNull(DoppelkopfScoresTable.COLUMN_GAME_ID)!!,
            round = cursor.getIntOrNull(DoppelkopfScoresTable.COLUMN_ROUND)!!,
            type = DoppelkopfScoreType.valueOf(
                cursor.getStringOrNull(DoppelkopfScoresTable.COLUMN_SCORE_TYPE)!!
            ),
            winner = DoppelkopfParty.valueOf(
                cursor.getStringOrNull(DoppelkopfScoresTable.COLUMN_WINNER)!!
            ),
            reParticipant1 = cursor.getUuidOrNull(DoppelkopfScoresTable.COLUMN_RE_PARTICIPANT_1),
            reParticipant2 = cursor.getUuidOrNull(DoppelkopfScoresTable.COLUMN_RE_PARTICIPANT_2),
            soloistId = cursor.getUuidOrNull(DoppelkopfScoresTable.COLUMN_SOLOIST_ID),
            soloKind = cursor.getStringOrNull(DoppelkopfScoresTable.COLUMN_SOLO_KIND)
                ?.let { DoppelkopfSoloKind.valueOf(it) },
            winLevel = DoppelkopfWinLevel.valueOf(
                cursor.getStringOrNull(DoppelkopfScoresTable.COLUMN_WIN_LEVEL)!!
            ),
            absage = cursor.getStringOrNull(DoppelkopfScoresTable.COLUMN_ABSAGE)
                ?.let { DoppelkopfWinLevel.valueOf(it) },
            reAnnounced = cursor.getIntOrNull(DoppelkopfScoresTable.COLUMN_RE_ANNOUNCED) == 1,
            kontraAnnounced =
                cursor.getIntOrNull(DoppelkopfScoresTable.COLUMN_KONTRA_ANNOUNCED) == 1,
            extraPointsRe =
                cursor.getIntOrNull(DoppelkopfScoresTable.COLUMN_EXTRA_POINTS_RE) ?: 0,
            extraPointsKontra =
                cursor.getIntOrNull(DoppelkopfScoresTable.COLUMN_EXTRA_POINTS_KONTRA) ?: 0,
        )
    }
}
