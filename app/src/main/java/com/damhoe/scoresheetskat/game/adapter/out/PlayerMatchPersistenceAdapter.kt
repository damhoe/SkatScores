package com.damhoe.scoresheetskat.game.adapter.out

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import com.damhoe.scoresheetskat.base.Result
import com.damhoe.scoresheetskat.game.adapter.out.models.PlayerMatchDTO
import com.damhoe.scoresheetskat.persistence.DbHelper
import javax.inject.Inject

class PlayerMatchPersistenceAdapter @Inject constructor(private val dbHelper: DbHelper) {
    /** @noinspection UnusedReturnValue
     */
    fun insertPlayerMatch(playerMatch: PlayerMatchDTO): Long {
        return try {
            val db = dbHelper.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_GAME_ID, playerMatch.gameId)
            contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID, playerMatch.playerId)
            contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_POSITION, playerMatch.position)
            db.insert(DbHelper.PLAYER_MATCH_TABLE_NAME, null, contentValues)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    @SuppressLint("Range")
    protected fun getPlayerMatch(id: Long): Result<PlayerMatchDTO> {
        var cursor: Cursor? = null
        return try {
            val db = dbHelper.readableDatabase
            cursor = db.rawQuery(
                "SELECT * FROM "
                        + DbHelper.PLAYER_MATCH_TABLE_NAME
                        + " WHERE "
                        + DbHelper.PLAYER_MATCH_COLUMN_ID + " = ? ", arrayOf(id.toString())
            )
            if (cursor.count > 0) {
                cursor.moveToFirst()
                val player = cursorToPlayerMatchDTO(cursor)
                Result.success(player)
            } else {
                Result.failure("PlayerMatch with id $id does not exist")
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            throw e
        } finally {
            cursor?.close()
        }
    }

    protected fun updatePlayerMatch(playerMatch: PlayerMatchDTO): Int {
        return try {
            val db = dbHelper.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_GAME_ID, playerMatch.gameId)
            contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID, playerMatch.playerId)
            contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_POSITION, playerMatch.position)
            val whereClause = DbHelper.PLAYER_MATCH_COLUMN_ID + " = ? "
            db.update(
                DbHelper.PLAYER_MATCH_TABLE_NAME,
                contentValues,
                whereClause,
                arrayOf(playerMatch.id.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    protected fun deletePlayerMatch(id: Long): Result<PlayerMatchDTO> {
        return try {
            val db = dbHelper.writableDatabase
            val getResult = getPlayerMatch(id)
            if (getResult.isFailure) {
                return Result.failure("PlayerMatch with id $id was not deleted because it did not exist")
            }
            db.execSQL(
                "DELETE FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                        + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_ID + " = ?",
                arrayOf(id.toString() + "")
            )
            val deletedPlayer = getResult.value
            Result.success(deletedPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun isValidPlayerId(playerId: Long): Boolean {
        var cursor: Cursor? = null
        return try {
            val db = dbHelper.readableDatabase
            cursor = db.rawQuery(
                "SELECT * FROM " + DbHelper.PLAYER_TABLE_NAME
                        + " WHERE " + DbHelper.PLAYER_COLUMN_ID + " = ? ",
                arrayOf(playerId.toString())
            )
            cursor.count > 0
        } catch (e: NullPointerException) {
            e.printStackTrace()
            throw e
        } finally {
            cursor?.close()
        }
    }

    fun deletePlayerMatchesForGame(gameId: Long): Result<List<PlayerMatchDTO>> {
        return try {
            val db = dbHelper.writableDatabase
            val matches = loadPlayerMatches(gameId)
            if (matches.size > 0) {
                db.execSQL(
                    "DELETE FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                            + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_GAME_ID + " = ?",
                    arrayOf(gameId.toString() + "")
                )
            }
            Result.success(matches)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun loadPlayerMatches(gameId: Long): List<PlayerMatchDTO> {
        var cursor: Cursor? = null
        val matches: MutableList<PlayerMatchDTO> = ArrayList()
        return try {
            val db = dbHelper.readableDatabase
            cursor = db.rawQuery(
                "SELECT * FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                        + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_GAME_ID + " = ? ",
                arrayOf(gameId.toString() + "")
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val playerMatch = cursorToPlayerMatchDTO(cursor)
                matches.add(playerMatch)
                cursor.moveToNext()
            }
            matches
        } catch (e: NullPointerException) {
            e.printStackTrace()
            throw e
        } finally {
            cursor?.close()
        }
    }

    companion object {
        @SuppressLint("Range")
        private fun cursorToPlayerMatchDTO(cursor: Cursor?): PlayerMatchDTO {
            val idIndex = cursor!!.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_ID)
            val gameIdIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_GAME_ID)
            val playerIdIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID)
            val positionIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_POSITION)
            val id = cursor.getLong(idIndex)
            val gameId = cursor.getLong(gameIdIndex)
            val playerId = cursor.getLong(playerIdIndex)
            val position = cursor.getInt(positionIndex)
            return PlayerMatchDTO(id, gameId, playerId, position)
        }
    }
}