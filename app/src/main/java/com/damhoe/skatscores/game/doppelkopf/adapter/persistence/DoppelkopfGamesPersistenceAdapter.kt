package com.damhoe.skatscores.game.doppelkopf.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DoppelkopfGamesAdapter"

@Singleton
class DoppelkopfGamesPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(game: DoppelkopfGameDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowId = insert(
                DoppelkopfGamesTable.TABLE_NAME,
                null,
                game.toContentValues(),
            )

            if (rowId > 0)
            {
                Log.d(TAG, "Inserted DoppelkopfGame: ${game.id}")
                Result.success(Unit)
            } else
            {
                Result.failure(IllegalStateException("Failed to insert DoppelkopfGame: ${game.id}"))
            }
        }
    }

    fun delete(id: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            delete(
                DoppelkopfGamesTable.TABLE_NAME,
                "${DoppelkopfGamesTable.COLUMN_ID} = ?",
                arrayOf(id.toString()),
            )

            Result.success(Unit)
        }
    }

    fun get(id: UUID): Result<DoppelkopfGameDto?>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${DoppelkopfGamesTable.TABLE_NAME}" +
                    " WHERE ${DoppelkopfGamesTable.COLUMN_ID} = ?"

            val gameDto = rawQuery(query, arrayOf(id.toString()))
                .mapToOneOrNull { cursor -> DoppelkopfGameDto.mapFrom(cursor) }

            Result.success(gameDto)
        }
    }

    fun getAll(): Result<List<DoppelkopfGameDto>>
    {
        return databaseHelper.readableDatabase.run {
            val games = rawQuery("SELECT * FROM ${DoppelkopfGamesTable.TABLE_NAME}", null)
                .mapToList { cursor -> DoppelkopfGameDto.mapFrom(cursor) }

            Result.success(games)
        }
    }

    fun update(game: DoppelkopfGameDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = update(
                DoppelkopfGamesTable.TABLE_NAME,
                game.toContentValues(),
                "${DoppelkopfGamesTable.COLUMN_ID} = ?",
                arrayOf(game.id.toString()),
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Updated DoppelkopfGame: ${game.id}")
                Result.success(Unit)
            } else
            {
                Result.failure(
                    IllegalStateException("No DoppelkopfGame found to update with ID: ${game.id}")
                )
            }
        }
    }
}
