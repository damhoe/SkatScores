package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.persistence.DatabaseConstants.PlayersTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import java.util.UUID
import javax.inject.Inject

class PlayerPersistenceAdapter @Inject constructor(
    private val databaseHelper: DatabaseHelper,
)
{
    private val writableDatabase = databaseHelper.writableDatabase
    private val readableDatabase = databaseHelper.readableDatabase

    fun insert(player: PlayerDto): Result<Unit>
    {
        return writableDatabase.run {
            insert(
                PlayersTable.TABLE_NAME, null, player.toContentValues()
            )

            Result.success(Unit)
        }
    }

    fun get(id: UUID): Result<PlayerDto?>
    {
        return readableDatabase.run {
            val selection = "${PlayersTable.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(id.toString())

            val cursor = query(
                PlayersTable.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null,
            )

            val playerDto = cursor.mapToOneOrNull { cursor ->
                PlayerDto.mapFrom(cursor)
            }

            Result.success(playerDto)
        }
    }

    fun updatePlayer(player: PlayerDto): Result<Unit>
    {
        return writableDatabase.run {
            val whereClause = PlayersTable.COLUMN_ID + " = ? "

            update(
                PlayersTable.TABLE_NAME,
                player.toContentValues(),
                whereClause,
                arrayOf(player.id.toString()),
            )

            Result.success(Unit)
        }
    }

    fun deletePlayer(id: UUID): Result<PlayerDto?>
    {
        return writableDatabase.run {
            get(id).onSuccess {
                val whereClause = "${PlayersTable.COLUMN_ID} = ?"
                delete(
                    PlayersTable.TABLE_NAME,
                    whereClause,
                    arrayOf(id.toString()),
                )
            }
        }
    }

    fun getGameCount(playerId: UUID): Result<Int>
    {
        return readableDatabase.run {
            val selection = "${SkatParticipantsTable.COLUMN_PLAYER_ID} = ?"
            val selectionArgs = arrayOf(playerId.toString())

            val cursor = query(
                SkatParticipantsTable.TABLE_NAME,
                arrayOf(SkatParticipantsTable.COLUMN_GAME_ID),
                selection,
                selectionArgs,
                null,
                null,
                null,
            )

            cursor.use {
                Result.success(it.count)
            }
        }
    }

    fun getAll(): Result<List<PlayerDto>>
    {
        return readableDatabase.run {
            val cursor = query(
                PlayersTable.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
            )

            val players = cursor.mapToList { cursor ->
                PlayerDto.mapFrom(cursor)
            }

            Result.success(players)
        }
    }
}
