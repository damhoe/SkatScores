package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.players

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import java.util.UUID
import javax.inject.Inject

private const val TAG = "DkParticipantsAdapter"

class DoppelkopfParticipantsPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(participants: List<DoppelkopfParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            participants.forEach { participant ->
                val rowId = insert(
                    DoppelkopfParticipantsTable.TABLE_NAME,
                    null,
                    participant.toContentValues(),
                )

                if (rowId <= 0)
                {
                    throw IllegalStateException(
                        "Failed to insert Doppelkopf participant: ${participant.id}"
                    )
                }
            }

            Result.success(Unit)
        }
    }

    /**
     * Drops every seat of a list.
     *
     * The foreign key declares ON DELETE CASCADE, but SQLite only honours that with foreign
     * key enforcement switched on, which it is not by default on Android. Left behind, these
     * rows would keep counting towards the player statistics of a list that no longer exists.
     */
    fun deleteAllOfGame(gameId: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            delete(
                DoppelkopfParticipantsTable.TABLE_NAME,
                "${DoppelkopfParticipantsTable.COLUMN_GAME_ID} = ?",
                arrayOf(gameId.toString()),
            )

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

    /** Seats keep their row, so a substitution does not move the scores already recorded. */
    fun update(participants: List<DoppelkopfParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            participants.forEach { participant ->
                val rowsAffected = update(
                    DoppelkopfParticipantsTable.TABLE_NAME,
                    participant.toUpdateContentValues(),
                    "${DoppelkopfParticipantsTable.COLUMN_SEAT} = ?" +
                            " AND ${DoppelkopfParticipantsTable.COLUMN_GAME_ID} = ?",
                    arrayOf(participant.seat.toString(), participant.gameId.toString()),
                )

                if (rowsAffected == 0)
                {
                    Log.w(TAG, "No row found to update for participant: ${participant.id}")
                }
            }

            Result.success(Unit)
        }
    }
}
