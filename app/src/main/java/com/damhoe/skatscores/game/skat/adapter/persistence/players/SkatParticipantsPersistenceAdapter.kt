package com.damhoe.skatscores.game.skat.adapter.persistence.players

import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesPersistenceAdapter.Companion.liveGamesCondition
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesPersistenceAdapter.Companion.sinceArgs
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.mapToList
import com.damhoe.skatscores.persistence.run
import com.damhoe.skatscores.persistence.updateStamped
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class SkatParticipantsPersistenceAdapter @Inject constructor(
    val databaseHelper: DatabaseHelper,
)
{
    fun insert(skatPlayers: List<SkatParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            skatPlayers.forEach { skatPlayer ->
                insertOrThrow(SkatParticipantsTable.TABLE_NAME, null, skatPlayer.toContentValues())
            }

            Result.success(Unit)
        }
    }

    fun getAllOfGame(gameId: UUID): Result<List<SkatParticipantDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query =
                "SELECT * FROM ${SkatParticipantsTable.TABLE_NAME} WHERE ${SkatParticipantsTable.COLUMN_GAME_ID} = ?"
            val selectionArgs = arrayOf(gameId.toString())

            val skatPlayers = rawQuery(query, selectionArgs)
                .mapToList { cursor ->
                    SkatParticipantDto.mapFromCursor(cursor)
                }

            Result.success(skatPlayers)
        }
    }

    /**
     * The seats of every list still there, in one query rather than one per list - the home
     * screen needs all of them at once.
     */
    fun getAllOfLiveGames(since: Instant? = null): Result<List<SkatParticipantDto>>
    {
        return databaseHelper.readableDatabase.run {
            val query = "SELECT p.* FROM ${SkatParticipantsTable.TABLE_NAME} p" +
                    " INNER JOIN ${SkatGamesTable.TABLE_NAME} g" +
                    " ON g.${SkatGamesTable.COLUMN_ID} = p.${SkatParticipantsTable.COLUMN_GAME_ID}" +
                    " WHERE ${liveGamesCondition(since, alias = "g")}"

            val participants = rawQuery(query, sinceArgs(since))
                .mapToList { cursor -> SkatParticipantDto.mapFromCursor(cursor) }

            Result.success(participants)
        }
    }

    /**
     * Writes who sits where, keeping each seat's row (and so its id, which the rounds point at).
     *
     * In two passes: names and player links are unique per list, so moving "Anna" from one seat
     * to another while "Ben" moves the other way would collide half-way through if written seat
     * by seat. The first pass parks every seat on a name nobody can type - its own id - and
     * unlinks it; the second writes the real values. A seat that is not there fails the whole
     * write rather than being skipped.
     */
    fun replaceSeats(skatPlayers: List<SkatParticipantDto>): Result<Unit>
    {
        return databaseHelper.writableDatabase.run {
            val seatCondition = "${SkatParticipantsTable.COLUMN_GAME_ID} = ?" +
                    " AND ${SkatParticipantsTable.COLUMN_TABLE_POSITION} = ?"

            fun seatArgs(dto: SkatParticipantDto) =
                arrayOf(dto.gameId.toString(), dto.tablePosition.toString())

            skatPlayers.forEach { dto ->
                val parked = update(
                    SkatParticipantsTable.TABLE_NAME,
                    contentValuesOf(
                        SkatParticipantsTable.COLUMN_NAME to "#${dto.id}",
                        SkatParticipantsTable.COLUMN_PLAYER_ID to null,
                    ),
                    seatCondition,
                    seatArgs(dto),
                )
                check(parked == 1) { "No seat ${dto.tablePosition} in list ${dto.gameId}" }
            }

            skatPlayers.forEach { dto ->
                updateStamped(
                    SkatParticipantsTable.TABLE_NAME,
                    dto.toUpdateContentValues(),
                    seatCondition,
                    seatArgs(dto),
                )
            }

            Result.success(Unit)
        }
    }
}
