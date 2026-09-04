package com.damhoe.skatscores.game.skat.adapter.persistence.players

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import java.util.UUID
import javax.inject.Inject

private const val TAG = "SkatParticipantsAdapter"

class SkatParticipantsPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(skatPlayers: List<SkatParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            skatPlayers.forEach { skatPlayer ->
                val rowId = insert(
                    SkatParticipantsTable.TABLE_NAME,
                    null,
                    skatPlayer.toContentValues()
                )

                if (rowId > 0)
                {
                    Log.d(TAG, "Inserted SkatPlayer: ${skatPlayer.id}")
                    Result.success(Unit)
                } else
                {
                    val errorMessage = "Failed to insert SkatPlayer: ${skatPlayer.id}"
                    throw IllegalStateException(errorMessage)
                }
            }

            Result.success(Unit)
        }
    }

    fun get(id: UUID): Result<SkatParticipantDto?>
    {
        return databaseHelper.readableDatabase.run {
            val query =
                "SELECT * FROM ${SkatParticipantsTable.TABLE_NAME} WHERE ${SkatParticipantsTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(id.toString())

            val skatPlayerDto = rawQuery(query, selectionArgs)
                .mapToOneOrNull { cursor ->
                    SkatParticipantDto.mapFromCursor(cursor)
                }

            Result.success(skatPlayerDto)
        }
    }

    fun delete(id: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val whereClause = "${SkatParticipantsTable.COLUMN_ID} = ?"
            val whereArgs = arrayOf(id.toString())

            val rowsAffected = this.delete(
                SkatParticipantsTable.TABLE_NAME,
                whereClause,
                whereArgs
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Deleted SkatPlayer with ID: $id. Rows affected: $rowsAffected")
                Result.success(Unit)
            } else
            {
                val errorMessage = "No SkatPlayer found with ID: $id to delete."
                Log.w(TAG, errorMessage)
                Result.failure(NoSuchElementException(errorMessage))
            }
        }
    }

    fun deleteAllOfGame(gameId: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val whereClause = "${SkatParticipantsTable.COLUMN_GAME_ID} = ?"
            val whereArgs = arrayOf(gameId.toString())

            val rowsAffected = this.delete(
                SkatParticipantsTable.TABLE_NAME,
                whereClause,
                whereArgs
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Deleted $rowsAffected SkatPlayers for Game ID: $gameId.")
                Result.success(Unit)
            } else
            {
                val errorMessage = "No SkatPlayers found for Game ID: $gameId to delete."
                Log.w(TAG, errorMessage)
                Result.failure(NoSuchElementException(errorMessage))
            }
        }
    }

    fun getAllOfGame(gameId: UUID): Result<List<SkatParticipantDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query =
                "SELECT * FROM ${SkatParticipantsTable.TABLE_NAME} WHERE ${SkatParticipantsTable.COLUMN_GAME_ID} = ?"
            val selectionArgs = arrayOf(gameId.toString())

            val skatPlayers = rawQuery(query, selectionArgs)
                .mapToList { cursor ->
                    SkatParticipantDto.mapFromCursor(cursor)
                }

            Result.success(skatPlayers)
        }
    }

    fun update(skatPlayers: List<SkatParticipantDto>): Result<Unit> {
        return try {
            databaseHelper.writableDatabase.run {
                try
                {
                    skatPlayers.forEach { skatPlayer ->
                        val whereClause = "${SkatParticipantsTable.COLUMN_TABLE_POSITION} = ? AND ${SkatParticipantsTable.COLUMN_GAME_ID} = ?"
                        val whereArgs = arrayOf(skatPlayer.tablePosition.toString(), skatPlayer.gameId.toString())

                        val rowsAffected = update(
                            SkatParticipantsTable.TABLE_NAME,
                            skatPlayer.toUpdateContentValues(),
                            whereClause,
                            whereArgs
                        )

                        if (rowsAffected == 0)
                        {
                            // This could happen if you try to update a participant
                            // that was somehow deleted or not yet inserted.
                            Log.w(TAG, "No row found to update for Participant: ${skatPlayer.id}")
                        }
                    }
                    Result.success(Unit)
                } finally
                {
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating participants", e)
            Result.failure(e)
        }
    }
}