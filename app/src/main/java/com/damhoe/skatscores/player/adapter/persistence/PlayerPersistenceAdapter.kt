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
 * Deleted profiles are tombstoned, never read, and free their name. Database handles are taken
 * per call: holding one would open the database on whichever thread builds this.
 */
class PlayerPersistenceAdapter @Inject constructor(
    private val databaseHelper: DatabaseHelper,
)
{
    private val seatTablesWithPlayerColumn = listOf(
        SkatParticipantsTable.TABLE_NAME to SkatParticipantsTable.COLUMN_PLAYER_ID,
        DoppelkopfParticipantsTable.TABLE_NAME to DoppelkopfParticipantsTable.COLUMN_PLAYER_ID,
    )

    /** Fails when another live profile has the name. */
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
     * Also renames the linked seats, which keep their own copy of the name. A list where another
     * seat already has the new name keeps the old one (still linked). Call inside a transaction.
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

            seatTablesWithPlayerColumn.forEach { (table, playerIdColumn) ->
                updateStamped(
                    table,
                    contentValuesOf(SEAT_NAME_COLUMN to player.name),
                    "$playerIdColumn = ?",
                    arrayOf(player.id.toString()),
                    ignoreConflicts = true,
                )
            }

            Result.success(Unit)
        }
    }

    /**
     * Its seats stay as guests. Done by hand because ON DELETE SET NULL never fires for a soft
     * delete. Call inside a transaction; null when there was no such profile.
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

            seatTablesWithPlayerColumn.forEach { (table, playerIdColumn) ->
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

    private companion object
    {
        // Same column name in both seat tables.
        const val SEAT_NAME_COLUMN = SkatParticipantsTable.COLUMN_NAME
    }
}
