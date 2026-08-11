package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SkatScoresAdapter"

@Singleton
class SkatScorePersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper
)
{
    fun getScoresForGame(gameId: UUID): List<SkatScoreDto>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${SkatScoresTable.TABLE_NAME}" +
                    " WHERE ${SkatScoresTable.COLUMN_GAME_ID} = ?" +
                    " ORDER BY ${SkatScoresTable.COLUMN_ROUND} ASC"
            val selectionArgs = arrayOf(gameId.toString())

            val games = rawQuery(query, selectionArgs)
                .mapToList { cursor ->
                    SkatScoreDto.mapFrom(cursor)
                }

            games
        }
    }

    fun insert(score: SkatScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowId = insert(
                SkatScoresTable.TABLE_NAME,
                null,
                score.toContentValues(),
            )

            if (rowId > 0)
            {
                Log.d(TAG, "Inserted SkatScore: ${score.id}")
                Result.success(Unit)
            } else
            {
                val errorMessage = "Failed to insert SkatScore: ${score.id}"
                Result.failure(IllegalStateException(errorMessage))
            }
        }
    }

    /**
     * Replaces a round in place. The row keeps its id and round number, so the score board
     * order and the unique (game_id, round) constraint are unaffected.
     */
    fun update(score: SkatScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val whereClause = "${SkatScoresTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(score.id.toString())

            val rowsAffected = update(
                SkatScoresTable.TABLE_NAME,
                score.toContentValues(),
                whereClause,
                selectionArgs,
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Updated SkatScore: ${score.id}")
                Result.success(Unit)
            } else
            {
                val errorMessage = "No SkatScore found to update with ID: ${score.id}"
                Result.failure(IllegalStateException(errorMessage))
            }
        }
    }

    fun delete(id: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val whereClause = "${SkatScoresTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(id.toString())

            val rowsAffected = delete(
                SkatScoresTable.TABLE_NAME,
                whereClause,
                selectionArgs,
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Deleted SkatScore: $id")
                Result.success(Unit)
            } else
            {
                Result.failure(NoSuchElementException("No SkatScore found with ID: $id"))
            }
        }
    }

    /**
     * Closes the gap left by a deleted round. Rounds are dense and zero-based, and
     * (game_id, round) is unique, so later rounds have to shift down.
     */
    fun shiftRoundsDown(gameId: UUID, fromRound: Int): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            execSQL(
                "UPDATE ${SkatScoresTable.TABLE_NAME}" +
                        " SET ${SkatScoresTable.COLUMN_ROUND} = ${SkatScoresTable.COLUMN_ROUND} - 1" +
                        " WHERE ${SkatScoresTable.COLUMN_GAME_ID} = ?" +
                        " AND ${SkatScoresTable.COLUMN_ROUND} > ?",
                arrayOf(gameId.toString(), fromRound)
            )

            Result.success(Unit)
        }
    }

    fun getRound(id: UUID): Result<Int?>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT ${SkatScoresTable.COLUMN_ROUND}" +
                    " FROM ${SkatScoresTable.TABLE_NAME}" +
                    " WHERE ${SkatScoresTable.COLUMN_ID} = ?"

            val round = rawQuery(query, arrayOf(id.toString()))
                .mapToOneOrNull { cursor -> cursor.getIntOrNull(SkatScoresTable.COLUMN_ROUND) }

            Result.success(round)
        }
    }
}