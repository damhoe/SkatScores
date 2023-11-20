package com.damhoe.scoresheetskat.score.adapter.out

import android.content.ContentValues
import android.database.Cursor
import com.damhoe.scoresheetskat.persistence.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Singleton
class ScorePersistenceAdapter @Inject constructor(private val dbHelper: DbHelper) {

    fun insertScore(score: ScoreDto): Result<Long> = try {
        // Get score position
        val maxScoreKey = "max_score"
        val scorePosition = dbHelper.readableDatabase.rawQuery(
            "SELECT MAX(${DbHelper.SCORE_COLUMN_POSITION}) AS $maxScoreKey " +
                    "FROM ${DbHelper.SCORE_TABLE_NAME} " +
                    "WHERE ${DbHelper.SCORE_COLUMN_GAME_ID} = ${score.gameId}", null
        ).use { cursor ->
            cursor.takeIf { it.count > 0 }?.run {
                moveToFirst()
                getIntOrDefault(maxScoreKey, -1) + 1
            } ?: 0
        }

        // Save data
        val contentValues = ContentValues().apply {
            put(DbHelper.SCORE_COLUMN_GAME_ID, score.gameId)
            put(DbHelper.SCORE_COLUMN_POSITION, scorePosition)
            put(DbHelper.SCORE_COLUMN_PLAYER_POSITION, score.playerPosition)
            put(DbHelper.SCORE_COLUMN_SPITZEN, score.spitzen)
            put(DbHelper.SCORE_COLUMN_SUIT, score.suit)
            put(DbHelper.SCORE_COLUMN_HAND, score.hand)
            put(DbHelper.SCORE_COLUMN_OUVERT, score.ouvert)
            put(DbHelper.SCORE_COLUMN_SCHNEIDER, score.schneider)
            put(DbHelper.SCORE_COLUMN_SCHNEIDER_ANNOUNCED, score.schneiderAnnounced)
            put(DbHelper.SCORE_COLUMN_SCHWARZ, score.schwarz)
            put(DbHelper.SCORE_COLUMN_SCHWARZ_ANNOUNCED, score.schwarzAnnounced)
            put(DbHelper.SCORE_COLUMN_RESULT, score.result)
        }

        dbHelper.writableDatabase.insert(
            DbHelper.SCORE_TABLE_NAME, null, contentValues
        ).let { success(it) }
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        failure(ex)
    }

    fun updateScore(score: ScoreDto): Result<Unit> = try {
        val contentValues = ContentValues().apply {
            put(DbHelper.SCORE_COLUMN_GAME_ID, score.gameId)
            put(DbHelper.SCORE_COLUMN_PLAYER_POSITION, score.playerPosition)
            put(DbHelper.SCORE_COLUMN_SPITZEN, score.spitzen)
            put(DbHelper.SCORE_COLUMN_SUIT, score.suit)
            put(DbHelper.SCORE_COLUMN_HAND, score.hand)
            put(DbHelper.SCORE_COLUMN_OUVERT, score.ouvert)
            put(DbHelper.SCORE_COLUMN_SCHNEIDER, score.schneider)
            put(DbHelper.SCORE_COLUMN_SCHNEIDER_ANNOUNCED, score.schneiderAnnounced)
            put(DbHelper.SCORE_COLUMN_SCHWARZ, score.schwarz)
            put(DbHelper.SCORE_COLUMN_SCHWARZ_ANNOUNCED, score.schwarzAnnounced)
            put(DbHelper.SCORE_COLUMN_RESULT, score.result)
        }

        val rowsAffected = dbHelper.writableDatabase.update(
            DbHelper.SCORE_TABLE_NAME,
            contentValues,
            "${DbHelper.SCORE_COLUMN_ID} = ${score.id}",
            null
        )

        if (rowsAffected == 1) {
            success(Unit)
        } else {
            failure(IllegalStateException("Unexpected result for " +
                    "score update: $rowsAffected rows affected."))
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        failure(ex)
    }


    fun getScoresForGame(gameId: Long): Result<List<ScoreDto>> = try {
        dbHelper.readableDatabase.rawQuery(
            "SELECT * FROM ${DbHelper.SCORE_TABLE_NAME} " +
                    "WHERE ${DbHelper.SCORE_COLUMN_GAME_ID} = $gameId " +
                    "ORDER BY ${DbHelper.SCORE_COLUMN_POSITION} ASC", null
        ).use { cursor ->
            val scores: MutableList<ScoreDto> = ArrayList()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                scores.add(cursor.toScoreDto())
                cursor.moveToNext()
            }
            success(scores)
        }
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        failure(ex)
    }

    fun getScore(id: Long): Result<ScoreDto> = try {
        dbHelper.readableDatabase.rawQuery(
            "SELECT * FROM ${DbHelper.SCORE_TABLE_NAME} " +
                    "WHERE ${DbHelper.SCORE_COLUMN_ID} = $id", null
        ).use { cursor ->
            cursor.takeIf { it.count >= 1 }?.run {
                moveToFirst()
                success(toScoreDto())
            } ?: failure(NoSuchElementException("Score with id $id does not exist."))
        }
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        failure(ex)
    }

    fun deleteScore(id: Long): Result<ScoreDto> = try {
        getScore(id).onSuccess {
            dbHelper.writableDatabase.execSQL(
                "DELETE FROM ${DbHelper.SCORE_TABLE_NAME} " +
                        " WHERE ${DbHelper.SCORE_COLUMN_ID} = $id", null
            )
        }
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        failure(ex)
    }

    fun deleteScoresForGame(gameId: Long): Result<Int> = try {
        dbHelper.writableDatabase.delete(DbHelper.SCORE_TABLE_NAME,
            "${DbHelper.SCORE_COLUMN_GAME_ID} = $gameId",
            null
        ).let { success(it) }
    } catch (ex: NullPointerException) {
        ex.printStackTrace()
        failure(ex)
    }

    private fun Cursor.toScoreDto() = ScoreDto().apply {
        id = getLongOrDefault(DbHelper.SCORE_COLUMN_ID, -1)
        gameId = getLongOrDefault(DbHelper.SCORE_COLUMN_GAME_ID, -1)
        playerPosition = getIntOrDefault(DbHelper.SCORE_COLUMN_PLAYER_POSITION, 0)
        spitzen = getIntOrDefault(DbHelper.SCORE_COLUMN_SPITZEN, 1)
        suit = getIntOrDefault(DbHelper.SCORE_COLUMN_SUIT, 1)
        hand = getIntOrDefault(DbHelper.SCORE_COLUMN_HAND, 0)
        ouvert = getIntOrDefault(DbHelper.SCORE_COLUMN_OUVERT, 0)
        schneider = getIntOrDefault(DbHelper.SCORE_COLUMN_SCHNEIDER, 0)
        schneiderAnnounced = getIntOrDefault(DbHelper.SCORE_COLUMN_SCHNEIDER_ANNOUNCED, 0)
        schwarz = getIntOrDefault(DbHelper.SCORE_COLUMN_SCHWARZ, 0)
        schwarzAnnounced = getIntOrDefault(DbHelper.SCORE_COLUMN_SCHWARZ_ANNOUNCED, 0)
        result = getIntOrDefault(DbHelper.SCORE_COLUMN_RESULT, 0)
    }
}