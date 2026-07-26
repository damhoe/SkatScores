package com.damhoe.skatscores.game.skat.adapter.persistence

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatPlayerCount
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import java.time.Instant
import java.util.UUID

data class SkatGameDto(
    val id: UUID,
    val title: String,
    val playedAt: Instant,
    val updatedAt: Instant?,
    val playerCount: SkatPlayerCount,

    // Settings
    val roundCount: Int,
    val scoringMode: SkatScoringMode,
)
{
    fun toContentValues() = contentValuesOf(
        SkatGamesTable.COLUMN_ID to id.toString(),
        SkatGamesTable.COLUMN_TITLE to title,
        SkatGamesTable.COLUMN_PLAYER_COUNT to playerCount.ordinal,
        SkatGamesTable.COLUMN_ROUND_COUNT to roundCount,
        SkatGamesTable.COLUMN_SCORING_MODE to scoringMode.name,
    )

    fun toSkatGame(
        participants: SkatParticipants,
        scores: List<SkatScore>,
    ): SkatGame
    {
        val settings = SkatSettings(
            roundCount = SkatRoundCount(roundCount),
            scoringMode = scoringMode,
        )
        return SkatGame(
            id = id,
            title = Title(title),
            playedAt = playedAt,
            settings = settings,
            participants = participants,
            scores = scores,
        )
    }

    companion object
    {
        fun mapFrom(game: SkatGame): SkatGameDto
        {
            return SkatGameDto(
                id = game.id,
                title = game.title.value,
                playedAt = game.playedAt,
                updatedAt = null,
                playerCount = SkatPlayerCount.THREE_PLAYERS,
                roundCount = game.settings.roundCount.value,
                scoringMode = game.settings.scoringMode,
            )
        }

        fun mapFrom(cursor: Cursor): SkatGameDto
        {
            val id = cursor.getUuidOrNull(SkatGamesTable.COLUMN_ID)!!
            val title = cursor.getStringOrNull(SkatGamesTable.COLUMN_TITLE)!!

            val playedAtString = cursor.getStringOrNull(SkatGamesTable.COLUMN_PLAYED_AT)
            val playedAt = Instant.parse(playedAtString)

            val updatedAtString = cursor.getStringOrNull(SkatGamesTable.COLUMN_UPDATED_AT)
            val updatedAt = Instant.parse(updatedAtString)

            val playerCountInt = cursor.getIntOrNull(SkatGamesTable.COLUMN_PLAYER_COUNT)!!
            val playerCount = SkatPlayerCount.entries[playerCountInt]

            val roundCount = cursor.getIntOrNull(SkatGamesTable.COLUMN_ROUND_COUNT)!!

            val scoringModeString = cursor.getStringOrNull(SkatGamesTable.COLUMN_SCORING_MODE)!!
            val scoringMode = SkatScoringMode.valueOf(scoringModeString)

            return SkatGameDto(
                id = id,
                title = title,
                playerCount = playerCount,
                playedAt = playedAt,
                updatedAt = updatedAt,
                roundCount = roundCount,
                scoringMode = scoringMode
            )
        }
    }
}
