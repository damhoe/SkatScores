package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import com.damhoe.skatscores.player.domain.PlayerName
import java.util.UUID

data class DoppelkopfParticipantDto(
    val id: UUID,
    val gameId: UUID,
    val playerId: UUID?,
    val seat: Int,
    val name: PlayerName,
)
{
    fun toContentValues() = contentValuesOf(
        DoppelkopfParticipantsTable.COLUMN_ID to id.toString(),
        DoppelkopfParticipantsTable.COLUMN_GAME_ID to gameId.toString(),
        DoppelkopfParticipantsTable.COLUMN_PLAYER_ID to playerId?.toString(),
        DoppelkopfParticipantsTable.COLUMN_SEAT to seat,
        DoppelkopfParticipantsTable.COLUMN_NAME to name.value,
    )

    fun toUpdateContentValues() = contentValuesOf(
        DoppelkopfParticipantsTable.COLUMN_PLAYER_ID to playerId?.toString(),
        DoppelkopfParticipantsTable.COLUMN_NAME to name.value,
    )

    fun toParticipant(): Participant = if (playerId == null)
    {
        Participant.Guest(id = id, name = name)
    } else
    {
        Participant.Registered(id = id, name = name, playerId = playerId)
    }

    companion object
    {
        fun mapFromCursor(cursor: Cursor): DoppelkopfParticipantDto
        {
            val id = cursor.getUuidOrNull(DoppelkopfParticipantsTable.COLUMN_ID)!!
            val gameId = cursor.getUuidOrNull(DoppelkopfParticipantsTable.COLUMN_GAME_ID)!!
            val playerId = cursor.getUuidOrNull(DoppelkopfParticipantsTable.COLUMN_PLAYER_ID)
            val seat = cursor.getIntOrNull(DoppelkopfParticipantsTable.COLUMN_SEAT)!!
            val name = cursor.getStringOrNull(DoppelkopfParticipantsTable.COLUMN_NAME)!!

            return DoppelkopfParticipantDto(id, gameId, playerId, seat, PlayerName(name))
        }

        fun mapFrom(
            participant: Participant,
            gameId: UUID,
            seat: Int,
        ): DoppelkopfParticipantDto
        {
            // Keep the participant id: scores reference it to find their score board column.
            return DoppelkopfParticipantDto(
                id = participant.id,
                gameId = gameId,
                playerId = (participant as? Participant.Registered)?.playerId,
                seat = seat,
                name = participant.name,
            )
        }

        fun mapManyFrom(
            participants: DoppelkopfParticipants,
            gameId: UUID,
        ): List<DoppelkopfParticipantDto> =
            participants.asList().mapIndexed { seat, participant ->
                mapFrom(participant, gameId, seat)
            }
    }
}
