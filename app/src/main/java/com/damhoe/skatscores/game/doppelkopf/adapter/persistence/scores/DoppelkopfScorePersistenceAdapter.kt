package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfScoresTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.getIntOrNull
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DoppelkopfScoresAdapter"

@Singleton
class DoppelkopfScorePersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun getScoresForGame(gameId: UUID): Result<List<DoppelkopfScoreDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${DoppelkopfScoresTable.TABLE_NAME}" +
                    " WHERE ${DoppelkopfScoresTable.COLUMN_GAME_ID} = ?" +
                    " ORDER BY ${DoppelkopfScoresTable.COLUMN_ROUND} ASC"

            val scores = rawQuery(query, arrayOf(gameId.toString()))
                .mapToList { cursor -> DoppelkopfScoreDto.mapFrom(cursor) }

            Result.success(scores)
        }
    }

    fun insert(score: DoppelkopfScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowId = insert(
                DoppelkopfScoresTable.TABLE_NAME,
                null,
                score.toContentValues(),
            )

            if (rowId > 0)
            {
                Log.d(TAG, "Inserted DoppelkopfScore: ${score.id}")
                Result.success(Unit)
            } else
            {
                Result.failure(
                    IllegalStateException("Failed to insert DoppelkopfScore: ${score.id}")
                )
            }
        }
    }

    /**
     * Replaces a round in place. The row keeps its id and round number, so the score board
     * order and the unique (game_id, round) constraint are unaffected.
     */
    fun update(score: DoppelkopfScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = update(
                DoppelkopfScoresTable.TABLE_NAME,
                score.toContentValues(),
                "${DoppelkopfScoresTable.COLUMN_ID} = ?",
                arrayOf(score.id.toString()),
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Updated DoppelkopfScore: ${score.id}")
                Result.success(Unit)
            } else
            {
                Result.failure(
                    IllegalStateException("No DoppelkopfScore found to update: ${score.id}")
                )
            }
        }
    }

    fun delete(id: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = delete(
                DoppelkopfScoresTable.TABLE_NAME,
                "${DoppelkopfScoresTable.COLUMN_ID} = ?",
                arrayOf(id.toString()),
            )

            if (rowsAffected > 0)
            {
                Result.success(Unit)
            } else
            {
                Result.failure(NoSuchElementException("No DoppelkopfScore found with ID: $id"))
            }
        }
    }

    /** Drops every round of a list; see the note on the participants adapter. */
    fun deleteAllOfGame(gameId: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            delete(
                DoppelkopfScoresTable.TABLE_NAME,
                "${DoppelkopfScoresTable.COLUMN_GAME_ID} = ?",
                arrayOf(gameId.toString()),
            )

            Result.success(Unit)
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
                "UPDATE ${DoppelkopfScoresTable.TABLE_NAME}" +
                        " SET ${DoppelkopfScoresTable.COLUMN_ROUND} = ${DoppelkopfScoresTable.COLUMN_ROUND} - 1" +
                        " WHERE ${DoppelkopfScoresTable.COLUMN_GAME_ID} = ?" +
                        " AND ${DoppelkopfScoresTable.COLUMN_ROUND} > ?",
                arrayOf<Any>(gameId.toString(), fromRound)
            )

            Result.success(Unit)
        }
    }

    fun getRound(id: UUID): Result<Int?>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT ${DoppelkopfScoresTable.COLUMN_ROUND}" +
                    " FROM ${DoppelkopfScoresTable.TABLE_NAME}" +
                    " WHERE ${DoppelkopfScoresTable.COLUMN_ID} = ?"

            val round = rawQuery(query, arrayOf(id.toString()))
                .mapToOneOrNull { cursor -> cursor.getIntOrNull(DoppelkopfScoresTable.COLUMN_ROUND) }

            Result.success(round)
        }
    }
}
