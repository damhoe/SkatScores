package com.damhoe.skatscores.game.skat.adapter.persistence

import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.Schema
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import com.damhoe.skatscores.persistence.softDelete
import com.damhoe.skatscores.persistence.updateStamped
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The `skat_games` rows. A deleted list keeps its row with `deleted_at` set, so every read here
 * leaves those out; see [SyncColumns].
 */
@Singleton
class SkatGamesPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(game: SkatGameDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            insertOrThrow(SkatGamesTable.TABLE_NAME, null, game.toContentValues())
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
                SkatGamesTable.TABLE_NAME,
                "${SkatGamesTable.COLUMN_ID} = ?",
                arrayOf(id.toString()),
            )
            Result.success(rows > 0)
        }
    }

    fun get(id: UUID): Result<SkatGameDto?>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${SkatGamesTable.TABLE_NAME}" +
                    " WHERE ${SkatGamesTable.COLUMN_ID} = ?" +
                    " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL"

            val gameDto = rawQuery(query, arrayOf(id.toString()))
                .mapToOneOrNull { cursor -> SkatGameDto.mapFrom(cursor) }

            Result.success(gameDto)
        }
    }

    /** Newest first; with [since], only the lists played from then on. */
    fun getAll(since: Instant? = null): Result<List<SkatGameDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${SkatGamesTable.TABLE_NAME}" +
                    " WHERE ${liveGamesCondition(since)}" +
                    " ORDER BY ${SkatGamesTable.COLUMN_PLAYED_AT} DESC"

            val games = rawQuery(query, sinceArgs(since))
                .mapToList { cursor -> SkatGameDto.mapFrom(cursor) }

            Result.success(games)
        }
    }

    /** Title and settings only; fails when the list is gone. */
    fun updateSettings(
        id: UUID,
        title: String,
        roundCount: Int,
        scoringMode: SkatScoringMode,
    ): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = updateStamped(
                SkatGamesTable.TABLE_NAME,
                SkatGameDto.settingsValues(title, roundCount, scoringMode),
                "${SkatGamesTable.COLUMN_ID} = ? AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
                arrayOf(id.toString()),
            )

            if (rowsAffected > 0) Result.success(Unit)
            else Result.failure(NoSuchElementException("No list $id to update"))
        }
    }

    companion object
    {
        /**
         * Which lists count as there, for this table and for the seat and round queries that
         * join onto it. [alias] is how the query names `skat_games`, if it does.
         */
        fun liveGamesCondition(since: Instant?, alias: String? = null): String
        {
            val prefix = alias?.let { "$it." }.orEmpty()
            val live = "$prefix${SyncColumns.COLUMN_DELETED_AT} IS NULL"
            return if (since == null) live
            else "$live AND $prefix${SkatGamesTable.COLUMN_PLAYED_AT} >= ?"
        }

        fun sinceArgs(since: Instant?): Array<String> =
            if (since == null) emptyArray() else arrayOf(Schema.timestamp(since))
    }
}
