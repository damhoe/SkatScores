package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.persistence.DatabaseConstants.PlayersTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import java.util.UUID
import javax.inject.Inject

/**
 * The database handles are taken per call rather than held in fields. Holding them opened the
 * database in the constructor - which on a first launch also runs every create script - on
 * whatever thread Hilt happened to build this on, which is the main one.
 */
class PlayerPersistenceAdapter @Inject constructor(
    private val databaseHelper: DatabaseHelper,
)
{
    /**
     * SQLite answers a rejected insert with -1 rather than an exception, so the row id has to
     * be checked. Without that a name the unique index refuses was reported as saved, and the
     * profile the caller thought it had created was never there.
     */
    fun insert(player: PlayerDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowId = insert(
                PlayersTable.TABLE_NAME, null, player.toContentValues()
            )

            if (rowId > 0) Result.success(Unit)
            else Result.failure(IllegalStateException("Failed to insert player ${player.id}"))
        }
    }

    fun get(id: UUID): Result<PlayerDto?>
    {
        return databaseHelper.readableDatabase.run {
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

    /** No rows updated means the name was refused or the profile is gone; either way it did
     * not happen, and reporting success would leave the new name on screen only. */
    fun updatePlayer(player: PlayerDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = update(
                PlayersTable.TABLE_NAME,
                player.toContentValues(),
                "${PlayersTable.COLUMN_ID} = ?",
                arrayOf(player.id.toString()),
            )

            if (rowsAffected > 0) Result.success(Unit)
            else Result.failure(IllegalStateException("Failed to update player ${player.id}"))
        }
    }

    fun deletePlayer(id: UUID): Result<PlayerDto?>
    {
        return databaseHelper.writableDatabase.run {
            get(id).mapCatching { player ->
                // Nothing to delete is not a failure, but a row that would not go is.
                player ?: return@mapCatching null

                val rowsAffected = delete(
                    PlayersTable.TABLE_NAME,
                    "${PlayersTable.COLUMN_ID} = ?",
                    arrayOf(id.toString()),
                )

                check(rowsAffected > 0) { "Failed to delete player $id" }
                player
            }
        }
    }

    fun getAll(): Result<List<PlayerDto>>
    {
        return databaseHelper.readableDatabase.run {
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
