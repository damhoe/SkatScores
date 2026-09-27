package com.damhoe.skatscores.game.doppelkopf.adapter.persistence

import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfScoringMode
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import com.damhoe.skatscores.persistence.softDelete
import com.damhoe.skatscores.persistence.updateStamped
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The `doppelkopf_games` rows. A deleted list keeps its row with `deleted_at` set, so every
 * read here leaves those out; see [SyncColumns].
 */
@Singleton
class DoppelkopfGamesPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(game: DoppelkopfGameDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            insertOrThrow(DoppelkopfGamesTable.TABLE_NAME, null, game.toContentValues())
            Result.success(Unit)
        }
    }

    /**
     * Marks the list deleted; its seats and rounds stay with it and are hidden along with it.
     * False when there was no such list, or it was already deleted.
     */
    fun softDelete(id: UUID): Result<Boolean>
    {
        return databaseHelper.writableDatabase.run {
            val rows = softDelete(
                DoppelkopfGamesTable.TABLE_NAME,
                "${DoppelkopfGamesTable.COLUMN_ID} = ?",
                arrayOf(id.toString()),
            )
            Result.success(rows > 0)
        }
    }

    fun get(id: UUID): Result<DoppelkopfGameDto?>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${DoppelkopfGamesTable.TABLE_NAME}" +
                    " WHERE ${DoppelkopfGamesTable.COLUMN_ID} = ?" +
                    " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL"

            val gameDto = rawQuery(query, arrayOf(id.toString()))
                .mapToOneOrNull { cursor -> DoppelkopfGameDto.mapFrom(cursor) }

            Result.success(gameDto)
        }
    }

    /** Newest first. */
    fun getAll(): Result<List<DoppelkopfGameDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${DoppelkopfGamesTable.TABLE_NAME}" +
                    " WHERE ${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " ORDER BY ${DoppelkopfGamesTable.COLUMN_PLAYED_AT} DESC"

            val games = rawQuery(query, null)
                .mapToList { cursor -> DoppelkopfGameDto.mapFrom(cursor) }

            Result.success(games)
        }
    }

    /** Title and settings only; fails when the list is gone. */
    fun updateSettings(
        id: UUID,
        title: String,
        roundCount: Int,
        scoringMode: DoppelkopfScoringMode,
    ): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = updateStamped(
                DoppelkopfGamesTable.TABLE_NAME,
                DoppelkopfGameDto.settingsValues(title, roundCount, scoringMode),
                "${DoppelkopfGamesTable.COLUMN_ID} = ?" +
                        " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
                arrayOf(id.toString()),
            )

            if (rowsAffected > 0) Result.success(Unit)
            else Result.failure(NoSuchElementException("No list $id to update"))
        }
    }
}
