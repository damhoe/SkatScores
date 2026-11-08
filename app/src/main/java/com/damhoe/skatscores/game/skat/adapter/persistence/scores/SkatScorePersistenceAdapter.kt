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

/** Rounds in `seq` order; undone rounds are tombstoned and never read. */
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

    /** Includes undone rounds, so a new round never takes an undone round's slot. */
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

    /** Keeps id and seq; fails when the round has been undone. */
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

    /** The id of the round taken back, or null when the list has none. */
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
