package com.damhoe.skatscores.game.skat.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SkatGamesAdapter"

@Singleton
class SkatGamesPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(game: SkatGameDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowId = insert(
                SkatGamesTable.TABLE_NAME,
                null,
                game.toContentValues(),
            )

            if (rowId > 0)
            {
                Log.d(TAG, "Inserted SkatGame: ${game.id}")
                Result.success(Unit)
            } else
            {
                val errorMessage = "Failed to insert SkatGame: ${game.id}"
                Result.failure(IllegalStateException(errorMessage))
            }
        }
    }

    fun delete(id: UUID): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val whereClause = "${SkatGamesTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(id.toString())

            delete(
                SkatGamesTable.TABLE_NAME,
                whereClause,
                selectionArgs,
            )

            Result.success(Unit)
        }
    }

    fun get(id: UUID): Result<SkatGameDto?>
    {
        return databaseHelper.readableDatabase.run {
            val query =
                "SELECT * FROM ${SkatGamesTable.TABLE_NAME} WHERE ${SkatGamesTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(id.toString())

            val gameDto = rawQuery(query, selectionArgs)
                .mapToOneOrNull { cursor ->
                    SkatGameDto.mapFrom(cursor)
                }

            Result.success(gameDto)
        }
    }

    fun getAll(): Result<List<SkatGameDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${SkatGamesTable.TABLE_NAME}"

            val games = rawQuery(query, null)
                .mapToList { cursor ->
                    SkatGameDto.mapFrom(cursor)
                }

            Result.success(games)
        }
    }

    fun update(game: SkatGameDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val whereClause = "${SkatGamesTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(game.id.toString())

            val rowsAffected = update(
                SkatGamesTable.TABLE_NAME,
                game.toContentValues(),
                whereClause,
                selectionArgs
            )

            if (rowsAffected > 0)
            {
                Log.d(TAG, "Updated SkatGame: ${game.id}")
                Result.success(Unit)
            } else
            {
                val errorMessage = "No SkatGame found to update with ID: ${game.id}"
                Result.failure(IllegalStateException(errorMessage))
            }
        }
    }
}
