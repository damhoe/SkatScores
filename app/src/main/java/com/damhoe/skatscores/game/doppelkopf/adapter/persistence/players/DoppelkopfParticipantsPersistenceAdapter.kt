package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players

import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.run
import com.damhoe.skatscores.persistence.updateStamped
import java.util.UUID
import javax.inject.Inject

class DoppelkopfParticipantsPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(participants: List<DoppelkopfParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            participants.forEach { participant ->
                insertOrThrow(
                    DoppelkopfParticipantsTable.TABLE_NAME,
                    null,
                    participant.toContentValues(),
                )
            }

            Result.success(Unit)
        }
    }

    fun getAllOfGame(gameId: UUID): Result<List<DoppelkopfParticipantDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${DoppelkopfParticipantsTable.TABLE_NAME}" +
                    " WHERE ${DoppelkopfParticipantsTable.COLUMN_GAME_ID} = ?" +
                    " ORDER BY ${DoppelkopfParticipantsTable.COLUMN_SEAT} ASC"

            val participants = rawQuery(query, arrayOf(gameId.toString()))
                .mapToList { cursor -> DoppelkopfParticipantDto.mapFromCursor(cursor) }

            Result.success(participants)
        }
    }

    fun getAllOfLiveGames(): Result<List<DoppelkopfParticipantDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT p.* FROM ${DoppelkopfParticipantsTable.TABLE_NAME} p" +
                    " INNER JOIN ${DoppelkopfGamesTable.TABLE_NAME} g" +
                    " ON g.${DoppelkopfGamesTable.COLUMN_ID} = p.${DoppelkopfParticipantsTable.COLUMN_GAME_ID}" +
                    " WHERE g.${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " ORDER BY p.${DoppelkopfParticipantsTable.COLUMN_SEAT} ASC"

            val participants = rawQuery(query, null)
                .mapToList { cursor -> DoppelkopfParticipantDto.mapFromCursor(cursor) }

            Result.success(participants)
        }
    }

    /** Keeps each seat's row id; two passes for the same reason as the Skat replaceSeats. */
    fun replaceSeats(participants: List<DoppelkopfParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val seatCondition = "${DoppelkopfParticipantsTable.COLUMN_GAME_ID} = ?" +
                    " AND ${DoppelkopfParticipantsTable.COLUMN_SEAT} = ?"

            fun seatArgs(dto: DoppelkopfParticipantDto) =
                arrayOf(dto.gameId.toString(), dto.seat.toString())

            participants.forEach { dto ->
                val parked = update(
                    DoppelkopfParticipantsTable.TABLE_NAME,
                    contentValuesOf(
                        DoppelkopfParticipantsTable.COLUMN_NAME to "#${dto.id}",
                        DoppelkopfParticipantsTable.COLUMN_PLAYER_ID to null,
                    ),
                    seatCondition,
                    seatArgs(dto),
                )
                check(parked == 1) { "No seat ${dto.seat} in list ${dto.gameId}" }
            }

            participants.forEach { dto ->
                updateStamped(
                    DoppelkopfParticipantsTable.TABLE_NAME,
                    dto.toUpdateContentValues(),
                    seatCondition,
                    seatArgs(dto),
                )
            }

            Result.success(Unit)
        }
    }
}
