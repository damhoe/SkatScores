package com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores

import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfScoresTable
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
 * The rounds of Doppelkopf lists, in `seq` order. An undone round keeps its row with
 * `deleted_at` set; every read here leaves those out.
 */
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
                    " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " ORDER BY ${DoppelkopfScoresTable.COLUMN_SEQ} ASC"

            val scores = rawQuery(query, arrayOf(gameId.toString()))
                .mapToList { cursor -> DoppelkopfScoreDto.mapFrom(cursor) }

            Result.success(scores)
        }
    }

    /** The rounds of every list still there, in one query; grouped by list by the caller. */
    fun getScoresOfLiveGames(): Result<List<DoppelkopfScoreDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT s.* FROM ${DoppelkopfScoresTable.TABLE_NAME} s" +
                    " INNER JOIN ${DoppelkopfGamesTable.TABLE_NAME} g" +
                    " ON g.${DoppelkopfGamesTable.COLUMN_ID} = s.${DoppelkopfScoresTable.COLUMN_GAME_ID}" +
                    " WHERE g.${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " AND s.${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " ORDER BY s.${DoppelkopfScoresTable.COLUMN_GAME_ID}," +
                    " s.${DoppelkopfScoresTable.COLUMN_SEQ}"

            val scores = rawQuery(query, null)
                .mapToList { cursor -> DoppelkopfScoreDto.mapFrom(cursor) }

            Result.success(scores)
        }
    }

    /**
     * The highest seq ever given to a round of this list, undone ones included, so a new round
     * never reuses the slot of one that was taken back.
     */
    fun lastSeq(gameId: UUID): Result<String?>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT MAX(${DoppelkopfScoresTable.COLUMN_SEQ})" +
                    " FROM ${DoppelkopfScoresTable.TABLE_NAME}" +
                    " WHERE ${DoppelkopfScoresTable.COLUMN_GAME_ID} = ?"

            val seq = rawQuery(query, arrayOf(gameId.toString()))
                .mapToOneOrNull { cursor -> if (cursor.isNull(0)) null else cursor.getString(0) }

            Result.success(seq)
        }
    }

    fun insert(score: DoppelkopfScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            insertOrThrow(DoppelkopfScoresTable.TABLE_NAME, null, score.toContentValues())
            Result.success(Unit)
        }
    }

    /**
     * Replaces a round in place. The row keeps its id and seq, so its position in the list
     * does not move. Fails when the round is gone - undone meanwhile, say.
     */
    fun update(score: DoppelkopfScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = updateStamped(
                DoppelkopfScoresTable.TABLE_NAME,
                score.toEditContentValues(),
                "${DoppelkopfScoresTable.COLUMN_ID} = ?" +
                        " AND ${DoppelkopfScoresTable.COLUMN_GAME_ID} = ?" +
                        " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
                arrayOf(score.id.toString(), score.gameId.toString()),
            )

            if (rowsAffected > 0) Result.success(Unit)
            else Result.failure(NoSuchElementException("No round ${score.id} to update"))
        }
    }

    /**
     * Takes back the newest round of the list, whichever that is in the database at the moment
     * this runs - not whichever a screen last showed. Null when the list has no rounds.
     */
    fun softDeleteLast(gameId: UUID): Result<UUID?>
    {
        return databaseHelper.writableDatabase.run {
            val lastId = rawQuery(
                "SELECT ${DoppelkopfScoresTable.COLUMN_ID} FROM ${DoppelkopfScoresTable.TABLE_NAME}" +
                        " WHERE ${DoppelkopfScoresTable.COLUMN_GAME_ID} = ?" +
                        " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                        " ORDER BY ${DoppelkopfScoresTable.COLUMN_SEQ} DESC LIMIT 1",
                arrayOf(gameId.toString()),
            ).mapToOneOrNull { cursor -> cursor.getString(0) }
                ?: return@run Result.success(null)

            softDelete(
                DoppelkopfScoresTable.TABLE_NAME,
                "${DoppelkopfScoresTable.COLUMN_ID} = ?",
                arrayOf(lastId),
            )

            Result.success(UUID.fromString(lastId))
        }
    }
}
