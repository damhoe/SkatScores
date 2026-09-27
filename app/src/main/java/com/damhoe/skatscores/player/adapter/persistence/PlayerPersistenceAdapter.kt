package com.damhoe.skatscores.player.adapter.persistence

import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.PlayersTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.mapToOneOrNull
import com.damhoe.skatscores.persistence.run
import com.damhoe.skatscores.persistence.softDelete
import com.damhoe.skatscores.persistence.updateStamped
import java.util.UUID
import javax.inject.Inject

/**
 * The `players` rows. A deleted profile keeps its row with `deleted_at` set, so every read here
 * leaves those out, and its name is free to be used again.
 *
 * The database handles are taken per call rather than held in fields. Holding them opened the
 * database in the constructor - which on a first launch also creates every table - on
 * whatever thread Hilt happened to build this on, which is the main one.
 */
class PlayerPersistenceAdapter @Inject constructor(
    private val databaseHelper: DatabaseHelper,
)
{
    /** The seat tables and their link column, for the writes a profile carries into lists. */
    private val seatTables = listOf(
        SkatParticipantsTable.TABLE_NAME to SkatParticipantsTable.COLUMN_PLAYER_ID,
        DoppelkopfParticipantsTable.TABLE_NAME to DoppelkopfParticipantsTable.COLUMN_PLAYER_ID,
    )

    /** A name already taken by another profile is refused by the unique index, as a failure. */
    fun insert(player: PlayerDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            insertOrThrow(PlayersTable.TABLE_NAME, null, player.toContentValues())
            Result.success(Unit)
        }
    }

    fun get(id: UUID): Result<PlayerDto?>
    {
        return databaseHelper.readableDatabase.run {
            val playerDto = rawQuery(
                "SELECT * FROM ${PlayersTable.TABLE_NAME}" +
                        " WHERE ${PlayersTable.COLUMN_ID} = ?" +
                        " AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
                arrayOf(id.toString()),
            ).mapToOneOrNull { cursor -> PlayerDto.mapFrom(cursor) }

            Result.success(playerDto)
        }
    }

    fun getAll(): Result<List<PlayerDto>>
    {
        return databaseHelper.readableDatabase.run {
            val players = rawQuery(
                "SELECT * FROM ${PlayersTable.TABLE_NAME}" +
                        " WHERE ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
                null,
            ).mapToList { cursor -> PlayerDto.mapFrom(cursor) }

            Result.success(players)
        }
    }

    /**
     * Renames the profile, and the seats linked to it with it: a seat carries its own copy of
     * the name, which is what its list shows and what the seats sheet matches on. A list where
     * somebody else already sits under the new name keeps the old one on that seat - still
     * linked, so it still counts towards this player.
     *
     * Call inside a transaction. No rows updated means the name was refused or the profile is
     * gone; either way it did not happen.
     */
    fun rename(player: PlayerDto): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val rowsAffected = updateStamped(
                PlayersTable.TABLE_NAME,
                contentValuesOf(PlayersTable.COLUMN_NAME to player.name),
                "${PlayersTable.COLUMN_ID} = ? AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
                arrayOf(player.id.toString()),
            )
            check(rowsAffected > 0) { "Failed to update player ${player.id}" }

            seatTables.forEach { (table, playerIdColumn) ->
                updateStamped(
                    table,
                    // Both seat tables call the column "name".
                    contentValuesOf(SkatParticipantsTable.COLUMN_NAME to player.name),
                    "$playerIdColumn = ?",
                    arrayOf(player.id.toString()),
                    conflict = "OR IGNORE",
                )
            }

            Result.success(Unit)
        }
    }

    /**
     * Deletes the profile and takes it off every list: the seats it sat in stay, under the same
     * name, as guests. The schema declares this (ON DELETE SET NULL), but a profile is only
     * marked deleted, never removed, so it has to be done here.
     *
     * Call inside a transaction. Returns the profile as it was, or null if there was none.
     */
    fun delete(id: UUID): Result<PlayerDto?>
    {
        return databaseHelper.writableDatabase.run {
            val player = get(id).getOrThrow() ?: return@run Result.success(null)

            softDelete(
                PlayersTable.TABLE_NAME,
                "${PlayersTable.COLUMN_ID} = ?",
                arrayOf(id.toString()),
            )

            seatTables.forEach { (table, playerIdColumn) ->
                updateStamped(
                    table,
                    contentValuesOf(playerIdColumn to null),
                    "$playerIdColumn = ?",
                    arrayOf(id.toString()),
                )
            }

            Result.success(player)
        }
    }
}
