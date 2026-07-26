package com.damhoe.skatscores.game.skat.adapter.persistence.players

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.skat.domain.SkatParticipant
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatPlayerPosition
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import com.damhoe.skatscores.player.domain.PlayerName
import java.util.UUID

data class SkatParticipantDto(
    val id: UUID,
    val gameId: UUID,
    val playerId: UUID?,
    val tablePosition: SkatPlayerPosition,
    val name: PlayerName,
)
{
    fun toContentValues() = contentValuesOf(
        SkatParticipantsTable.COLUMN_ID to id.toString(),
        SkatParticipantsTable.COLUMN_GAME_ID to gameId.toString(),
        SkatParticipantsTable.COLUMN_PLAYER_ID to playerId?.toString(),
        SkatParticipantsTable.COLUMN_TABLE_POSITION to tablePosition.toString(),
        SkatParticipantsTable.COLUMN_NAME to name.value,
    )

    fun toUpdateContentValues() = contentValuesOf(
        SkatParticipantsTable.COLUMN_PLAYER_ID to playerId?.toString(),
        SkatParticipantsTable.COLUMN_NAME to name.value,
    )

    fun toSkatParticipant(): SkatParticipant
    {
        return if (playerId == null)
        {
            SkatParticipant.Guest(
                id = id,
                name = name
            )
        } else
        {
            SkatParticipant.Registered(
                id = id,
                name = name,
                playerId = playerId
            )
        }
    }

    companion object
    {
        fun mapFromCursor(cursor: Cursor): SkatParticipantDto
        {
            val id = cursor.getUuidOrNull(SkatParticipantsTable.COLUMN_ID)!!
            val gameId = cursor.getUuidOrNull(SkatParticipantsTable.COLUMN_GAME_ID)!!
            val playerId = cursor.getUuidOrNull(SkatParticipantsTable.COLUMN_PLAYER_ID)
            val tablePosition = SkatPlayerPosition.valueOf(
                cursor.getStringOrNull(SkatParticipantsTable.COLUMN_TABLE_POSITION)!!
            )
            val name = cursor.getStringOrNull(SkatParticipantsTable.COLUMN_NAME)!!

            return SkatParticipantDto(
                id, gameId, playerId, tablePosition, PlayerName(name)
            )
        }

        fun mapFrom(
            participant: SkatParticipant,
            gameId: UUID,
            tablePosition: SkatPlayerPosition,
        ): SkatParticipantDto
        {
            // Keep the participant id: scores reference it to find their score board column.
            return SkatParticipantDto(
                id = participant.id,
                gameId = gameId,
                playerId = if (participant is SkatParticipant.Registered) participant.playerId else null,
                tablePosition = tablePosition,
                name = participant.name,
            )
        }

        fun mapManyFrom(
            participants: SkatParticipants,
            gameId: UUID
        ): List<SkatParticipantDto>
        {
            val forehand = mapFrom(
                participant = participants.foreHand,
                gameId = gameId,
                tablePosition = SkatPlayerPosition.FOREHAND
            )
            val middlehand = mapFrom(
                participant = participants.middleHand,
                gameId = gameId,
                tablePosition = SkatPlayerPosition.MIDDLEHAND
            )
            val rearhand = mapFrom(
                participant = participants.rearHand,
                gameId = gameId,
                tablePosition = SkatPlayerPosition.REARHAND
            )

            return listOf(forehand, middlehand, rearhand)
        }
    }
}
