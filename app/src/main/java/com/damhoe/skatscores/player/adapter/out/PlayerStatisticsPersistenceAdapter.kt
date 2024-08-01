package com.damhoe.skatscores.player.adapter.out

import com.damhoe.skatscores.game.domain.score.SkatOutcome
import com.damhoe.skatscores.persistence.DbHelper
import javax.inject.Inject

class PlayerStatisticsPersistenceAdapter @Inject constructor(private val helper: DbHelper) {

    fun getListCount(playerId: Long): Int {
        helper.readableDatabase.use { db ->
            db.query(
                DbHelper.PLAYER_MATCH_TABLE_NAME,
                null,
                "${DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID} = $playerId",
                null, null, null, null
            ).use { return it.count }
        }
    }

    fun getRoundCount(playerId: Long): Int {
        helper.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DbHelper.SCORE_TABLE_NAME} " +
                        "INNER JOIN ${DbHelper.PLAYER_MATCH_TABLE_NAME} " +
                        "ON ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_GAME_ID} = " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_GAME_ID} " +
                        "WHERE ${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID} = $playerId",
                null, null
            ).use {
                if (it.moveToFirst()) {
                    return it.getInt(0)
                }
                return 0
            }
        }
    }

    fun getSoloCount(playerId: Long): Int {
        helper.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DbHelper.SCORE_TABLE_NAME} " +
                        "INNER JOIN ${DbHelper.PLAYER_MATCH_TABLE_NAME} " +
                        "ON ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_GAME_ID} = " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_GAME_ID} " +
                        "WHERE ${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID} = $playerId " +
                        "AND ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_PLAYER_POSITION} = " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_POSITION}",
                null, null
            ).use {
                if (it.moveToFirst()) {
                    return it.getInt(0)
                }
                return 0
            }
        }
    }

    fun getSoloWinsCount(playerId: Long): Int {
        helper.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DbHelper.SCORE_TABLE_NAME} " +
                        "INNER JOIN ${DbHelper.PLAYER_MATCH_TABLE_NAME} " +
                        "ON ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_GAME_ID} = " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_GAME_ID} " +
                        "WHERE ${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID} = $playerId " +
                        "AND ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_PLAYER_POSITION} = " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_POSITION} " +
                        "AND ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_RESULT} = ${SkatOutcome.WON.asInteger()}",
                null, null
            ).use {
                if (it.moveToFirst()) {
                    return it.getInt(0)
                }
                return 0
            }
        }
    }

    fun getAgainstWinsCount(playerId: Long): Int {
        helper.readableDatabase.use { db ->
            db.rawQuery(
                "SELECT COUNT(*) FROM ${DbHelper.SCORE_TABLE_NAME} " +
                        "INNER JOIN ${DbHelper.PLAYER_MATCH_TABLE_NAME} " +
                        "ON ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_GAME_ID} = " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_GAME_ID} " +
                        "WHERE ${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID} = $playerId " +
                        "AND ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_PLAYER_POSITION} != " +
                        "${DbHelper.PLAYER_MATCH_TABLE_NAME}.${DbHelper.PLAYER_MATCH_COLUMN_POSITION} " +
                        "AND ${DbHelper.SCORE_TABLE_NAME}.${DbHelper.SCORE_COLUMN_RESULT} = ${SkatOutcome.LOST.asInteger()}",
                null, null
            ).use {
                if (it.moveToFirst()) {
                    return it.getInt(0)
                }
                return 0
            }
        }
    }
}