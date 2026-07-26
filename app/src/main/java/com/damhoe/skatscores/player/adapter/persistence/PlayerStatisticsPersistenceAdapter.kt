package com.damhoe.skatscores.player.adapter.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.persistence.DatabaseConstants
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.run
import com.damhoe.skatscores.player.domain.PlayerStatistics
import java.util.UUID
import javax.inject.Inject

class PlayerStatisticsPersistenceAdapter @Inject constructor(
    private val dbHelper: DatabaseHelper
)
{
    private val readableDatabase = dbHelper.readableDatabase

    fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics>
    {
        return try
        {
            readableDatabase.run {
                val totalGamesPlayed = getTotalGamesPlayedInternal(this, playerId)
                val totalRoundsPlayed = getTotalRoundsPlayedInternal(this, playerId)
                val soloRoundsPlayed = getSoloRoundsPlayedInternal(this, playerId)
                val soloRoundsWon = getSoloRoundsWonInternal(this, playerId)
                val roundsWonAsOpponent = getRoundsWonAsOpponentInternal(this, playerId)

                val playerStats = PlayerStatistics(
                    totalGamesPlayed = totalGamesPlayed,
                    totalRoundsPlayed = totalRoundsPlayed,
                    soloRoundsPlayed = soloRoundsPlayed,
                    soloRoundsWon = soloRoundsWon,
                    roundsWonAsOpponent = roundsWonAsOpponent
                )

                Result.success(playerStats)
            }
        } catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    fun getGameCount(playerId: UUID): Result<Int>
    {
        return try
        {
            readableDatabase.run {
                Result.success(getTotalGamesPlayedInternal(this, playerId))
            }
        } catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    private fun executeCountQuery(
        db: SQLiteDatabase,
        query: String,
        selectionArgs: Array<String>
    ): Int
    {
        db.rawQuery(query, selectionArgs).use { cursor ->
            if (cursor.moveToFirst())
            {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    private fun getTotalRoundsPlayedInternal(db: SQLiteDatabase, playerId: UUID): Int
    {
        val query = ("""
            SELECT COUNT(*)
            FROM ${DatabaseConstants.SkatScoresTable.TABLE_NAME} scores
            INNER JOIN ${DatabaseConstants.SkatParticipantsTable.TABLE_NAME} players
            ON scores.${DatabaseConstants.SkatScoresTable.COLUMN_GAME_ID} = players.${DatabaseConstants.SkatParticipantsTable.COLUMN_GAME_ID}
            WHERE players.${DatabaseConstants.SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
        """).trimIndent()
        return executeCountQuery(db, query, arrayOf(playerId.toString()))
    }

    private fun getTotalGamesPlayedInternal(db: SQLiteDatabase, playerId: UUID): Int
    {
        val query = ("""
            SELECT COUNT(*)
            FROM ${DatabaseConstants.SkatParticipantsTable.TABLE_NAME} players
            WHERE players.${DatabaseConstants.SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
        """).trimIndent()
        return executeCountQuery(db, query, arrayOf(playerId.toString()))
    }

    private fun getSoloRoundsPlayedInternal(db: SQLiteDatabase, playerId: UUID): Int
    {
        val query = ("""
            SELECT COUNT(*)
            FROM ${DatabaseConstants.SkatScoresTable.TABLE_NAME} scores
            INNER JOIN ${DatabaseConstants.SkatParticipantsTable.TABLE_NAME} players
            ON scores.${DatabaseConstants.SkatScoresTable.COLUMN_GAME_ID} = players.${DatabaseConstants.SkatParticipantsTable.COLUMN_GAME_ID}
            WHERE players.${DatabaseConstants.SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
            AND scores.${DatabaseConstants.SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID} = players.${DatabaseConstants.SkatParticipantsTable.COLUMN_TABLE_POSITION}
        """).trimIndent()
        return executeCountQuery(db, query, arrayOf(playerId.toString()))
    }

    private fun getSoloRoundsWonInternal(db: SQLiteDatabase, playerId: UUID): Int
    {
        val query = ("""
            SELECT COUNT(*)
            FROM ${DatabaseConstants.SkatScoresTable.TABLE_NAME} scores
            INNER JOIN ${DatabaseConstants.SkatParticipantsTable.TABLE_NAME} players
            ON scores.${DatabaseConstants.SkatScoresTable.COLUMN_GAME_ID} = players.${DatabaseConstants.SkatParticipantsTable.COLUMN_GAME_ID}
            WHERE players.${DatabaseConstants.SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
            AND scores.${DatabaseConstants.SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID} = players.${DatabaseConstants.SkatParticipantsTable.COLUMN_TABLE_POSITION}
            AND scores.${DatabaseConstants.SkatScoresTable.COLUMN_WON_OR_LOST} = ?
        """).trimIndent()
        return executeCountQuery(db, query, arrayOf(playerId.toString(), WonOrLost.WON.toString()))
    }

    private fun getRoundsWonAsOpponentInternal(db: SQLiteDatabase, playerId: UUID): Int
    {
        val query = ("""
            SELECT COUNT(*)
            FROM ${DatabaseConstants.SkatScoresTable.TABLE_NAME} scores
            INNER JOIN ${DatabaseConstants.SkatParticipantsTable.TABLE_NAME} players
            ON scores.${DatabaseConstants.SkatScoresTable.COLUMN_GAME_ID} = players.${DatabaseConstants.SkatParticipantsTable.COLUMN_GAME_ID}
            WHERE players.${DatabaseConstants.SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
            AND scores.${DatabaseConstants.SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID} != players.${DatabaseConstants.SkatParticipantsTable.COLUMN_TABLE_POSITION}
            AND scores.${DatabaseConstants.SkatScoresTable.COLUMN_WON_OR_LOST} = ? 
        """).trimIndent()
        // Player is opponent & soloist LOST (meaning opponent team won)
        return executeCountQuery(db, query, arrayOf(playerId.toString(), WonOrLost.LOST.toString()))
    }
}