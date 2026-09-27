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

/** Rounds in `seq` order; undone rounds are tombstoned and never read. */
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

    /** Includes undone rounds, so a new round never takes an undone round's slot. */
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

    /** Keeps id and seq; fails when the round has been undone. */
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

    /** The id of the round taken back, or null when the list has none. */
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
