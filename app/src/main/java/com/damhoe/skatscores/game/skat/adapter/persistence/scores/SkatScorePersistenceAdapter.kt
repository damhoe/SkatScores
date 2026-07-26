package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
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
            val query = "SELECT * FROM ${SkatScoresTable.TABLE_NAME} WHERE ${SkatScoresTable.COLUMN_GAME_ID} = ?"
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
}