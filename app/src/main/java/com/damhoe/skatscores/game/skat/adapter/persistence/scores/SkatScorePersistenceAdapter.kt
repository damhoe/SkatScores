package com.damhoe.skatscores.game.skat.adapter.persistence.scores

import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesPersistenceAdapter.Companion.liveGamesCondition
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesPersistenceAdapter.Companion.sinceArgs
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns
import com.damhoe.skatscores.persistence.DatabaseHelper
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
 * The rounds of Skat lists, in `seq` order. An undone round keeps its row with `deleted_at`
 * set; every read here leaves those out.
 */
@Singleton
class SkatScorePersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper
)
{
    fun getScoresForGame(gameId: UUID): Result<List<SkatScoreDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT * FROM ${SkatScoresTable.TABLE_NAME}" +
                    " WHERE ${SkatScoresTable.COLUMN_GAME_ID} = ?" +
                    " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " ORDER BY ${SkatScoresTable.COLUMN_SEQ} ASC"

            val scores = rawQuery(query, arrayOf(gameId.toString()))
                .mapToList { cursor -> SkatScoreDto.mapFrom(cursor) }

            Result.success(scores)
        }
    }

    /** The rounds of every list still there, in one query; grouped by list by the caller. */
    fun getScoresOfLiveGames(since: Instant? = null): Result<List<SkatScoreDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT s.* FROM ${SkatScoresTable.TABLE_NAME} s" +
                    " INNER JOIN ${SkatGamesTable.TABLE_NAME} g" +
                    " ON g.${SkatGamesTable.COLUMN_ID} = s.${SkatScoresTable.COLUMN_GAME_ID}" +
                    " WHERE ${liveGamesCondition(since, alias = "g")}" +
                    " AND s.${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                    " ORDER BY s.${SkatScoresTable.COLUMN_GAME_ID}, s.${SkatScoresTable.COLUMN_SEQ}"

            val scores = rawQuery(query, sinceArgs(since))
                .mapToList { cursor -> SkatScoreDto.mapFrom(cursor) }

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
            val query = "SELECT MAX(${SkatScoresTable.COLUMN_SEQ})" +
                    " FROM ${SkatScoresTable.TABLE_NAME}" +
                    " WHERE ${SkatScoresTable.COLUMN_GAME_ID} = ?"

            val seq = rawQuery(query, arrayOf(gameId.toString()))
                .mapToOneOrNull { cursor -> if (cursor.isNull(0)) null else cursor.getString(0) }

            Result.success(seq)
        }
    }

    fun insert(score: SkatScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            insertOrThrow(SkatScoresTable.TABLE_NAME, null, score.toContentValues())
            Result.success(Unit)
        }
    }

    /**
     * Replaces a round in place. The row keeps its id and seq, so its position in the list
     * does not move. Fails when the round is gone - undone meanwhile, say.
     */
    fun update(score: SkatScoreDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = updateStamped(
                SkatScoresTable.TABLE_NAME,
                score.toEditContentValues(),
                "${SkatScoresTable.COLUMN_ID} = ?" +
                        " AND ${SkatScoresTable.COLUMN_GAME_ID} = ?" +
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
                "SELECT ${SkatScoresTable.COLUMN_ID} FROM ${SkatScoresTable.TABLE_NAME}" +
                        " WHERE ${SkatScoresTable.COLUMN_GAME_ID} = ?" +
                        " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL" +
                        " ORDER BY ${SkatScoresTable.COLUMN_SEQ} DESC LIMIT 1",
                arrayOf(gameId.toString()),
            ).mapToOneOrNull { cursor -> cursor.getString(0) }
                ?: return@run Result.success(null)

            softDelete(
                SkatScoresTable.TABLE_NAME,
                "${SkatScoresTable.COLUMN_ID} = ?",
                arrayOf(lastId),
            )

            Result.success(UUID.fromString(lastId))
        }
    }
}
