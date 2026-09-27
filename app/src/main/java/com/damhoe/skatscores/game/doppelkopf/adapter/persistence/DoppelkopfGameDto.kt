package com.damhoe.skatscores.game.doppelkopf.adapter.persistence

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGame
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfRoundCount
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfScoringMode
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.Schema
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import java.time.Instant
import java.util.UUID

data class DoppelkopfGameDto(
    val id: UUID,
    val title: String,
    val playedAt: Instant,

    // Settings
    val roundCount: Int,
    val scoringMode: DoppelkopfScoringMode,
)
{
    fun toContentValues() = contentValuesOf(
        DoppelkopfGamesTable.COLUMN_ID to id.toString(),
        DoppelkopfGamesTable.COLUMN_PLAYED_AT to Schema.timestamp(playedAt),
    ).apply { putAll(settingsValues(title, roundCount, scoringMode)) }

    fun toDoppelkopfGame(
        participants: DoppelkopfParticipants,
        scores: List<DoppelkopfScore>,
    ) = DoppelkopfGame(
        id = id,
        title = Title(title),
        playedAt = playedAt,
        settings = DoppelkopfSettings(
            roundCount = DoppelkopfRoundCount(roundCount),
            scoringMode = scoringMode,
        ),
        participants = participants,
        scores = scores,
    )

    companion object
    {
        /** What the settings sheet can change; the rest of the row is fixed once written. */
        fun settingsValues(title: String, roundCount: Int, scoringMode: DoppelkopfScoringMode) =
            contentValuesOf(
                DoppelkopfGamesTable.COLUMN_TITLE to title,
                DoppelkopfGamesTable.COLUMN_ROUND_COUNT to roundCount,
                DoppelkopfGamesTable.COLUMN_SCORING_MODE to scoringMode.name,
            )

        fun mapFrom(game: DoppelkopfGame) = DoppelkopfGameDto(
            id = game.id,
            title = game.title.value,
            playedAt = game.playedAt,
            roundCount = game.settings.roundCount.value,
            scoringMode = game.settings.scoringMode,
        )

        fun mapFrom(cursor: Cursor): DoppelkopfGameDto
        {
            val id = cursor.getUuidOrNull(DoppelkopfGamesTable.COLUMN_ID)!!
            val title = cursor.getStringOrNull(DoppelkopfGamesTable.COLUMN_TITLE)!!

            val playedAt =
                Instant.parse(cursor.getStringOrNull(DoppelkopfGamesTable.COLUMN_PLAYED_AT))

            val roundCount = cursor.getIntOrNull(DoppelkopfGamesTable.COLUMN_ROUND_COUNT)!!

            val scoringMode = DoppelkopfScoringMode.valueOf(
                cursor.getStringOrNull(DoppelkopfGamesTable.COLUMN_SCORING_MODE)!!
            )

            return DoppelkopfGameDto(
                id = id,
                title = title,
                playedAt = playedAt,
                roundCount = roundCount,
                scoringMode = scoringMode,
            )
        }
    }
}
