package com.damhoe.skatscores.player.adapter.persistence

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores.DoppelkopfScoreType
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreType
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfScoresTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.player.domain.DoppelkopfPlayerStatistics
import com.damhoe.skatscores.player.domain.ListCounts
import com.damhoe.skatscores.player.domain.PlayerStatistics
import com.damhoe.skatscores.player.domain.SkatPlayerStatistics
import java.util.UUID
import javax.inject.Inject

/**
 * A player's record, read straight out of the two sets of score tables.
 *
 * Both games link their seats to the same `players` row, so every query here starts from the
 * seats carrying this player's id and joins that list's rounds onto them. Deleted lists and
 * undone rounds are left out. Each game's record is one query adding up conditional counts,
 * rather than a query per number as it used to be.
 */
class PlayerStatisticsPersistenceAdapter @Inject constructor(
    private val dbHelper: DatabaseHelper,
)
{
    fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics> = dbHelper.read { db ->
        PlayerStatistics(
            skat = readSkatStatistics(db, playerId),
            doppelkopf = readDoppelkopfStatistics(db, playerId),
        )
    }

    /**
     * Lists each registered player appears in, per game, for everybody at once: the players
     * screen shows all of them, and asked one player at a time that was two queries a player.
     * Players without a list are absent from the map.
     */
    fun getAllListCounts(): Result<Map<UUID, ListCounts>> = dbHelper.read { db ->
        val skat = countListsPerPlayer(
            db,
            SkatParticipantsTable.TABLE_NAME,
            SkatParticipantsTable.COLUMN_PLAYER_ID,
            SkatParticipantsTable.COLUMN_GAME_ID,
            SkatGamesTable.TABLE_NAME,
        )
        val doppelkopf = countListsPerPlayer(
            db,
            DoppelkopfParticipantsTable.TABLE_NAME,
            DoppelkopfParticipantsTable.COLUMN_PLAYER_ID,
            DoppelkopfParticipantsTable.COLUMN_GAME_ID,
            DoppelkopfGamesTable.TABLE_NAME,
        )

        (skat.keys + doppelkopf.keys).associateWith { playerId ->
            ListCounts(skat = skat[playerId] ?: 0, doppelkopf = doppelkopf[playerId] ?: 0)
        }
    }

    private fun countListsPerPlayer(
        db: SQLiteDatabase,
        participantsTable: String,
        playerIdColumn: String,
        gameIdColumn: String,
        gamesTable: String,
    ): Map<UUID, Int> = db.rawQuery(
        """
            SELECT p.$playerIdColumn, COUNT(*)
            FROM $participantsTable p
            INNER JOIN $gamesTable g ON g.id = p.$gameIdColumn
            WHERE p.$playerIdColumn IS NOT NULL
            AND g.${SyncColumns.COLUMN_DELETED_AT} IS NULL
            GROUP BY p.$playerIdColumn
        """.trimIndent(),
        null,
    ).use { cursor ->
        buildMap {
            while (cursor.moveToNext())
            {
                put(UUID.fromString(cursor.getString(0)), cursor.getInt(1))
            }
        }
    }

    // --- Skat -----------------------------------------------------------------------------

    private fun readSkatStatistics(db: SQLiteDatabase, playerId: UUID): SkatPlayerStatistics
    {
        val listsPlayed = countLists(
            db,
            playerId,
            SkatParticipantsTable.TABLE_NAME,
            SkatGamesTable.TABLE_NAME,
        )

        return db.rawQuery(
            """
                SELECT
                    COUNT(*),
                    ${countIf(PLAYER_DECLARED)},
                    ${countIf("$PLAYER_DECLARED AND $SKAT_DECLARER_WON")},
                    ${countIf("$PLAYER_DEFENDED AND $SKAT_DECLARER_LOST")}
                FROM ${SkatParticipantsTable.TABLE_NAME} participants
                INNER JOIN ${SkatGamesTable.TABLE_NAME} games
                    ON games.${SkatGamesTable.COLUMN_ID} = participants.${SkatParticipantsTable.COLUMN_GAME_ID}
                    AND games.${SyncColumns.COLUMN_DELETED_AT} IS NULL
                INNER JOIN ${SkatScoresTable.TABLE_NAME} scores
                    ON scores.${SkatScoresTable.COLUMN_GAME_ID} = participants.${SkatParticipantsTable.COLUMN_GAME_ID}
                    AND scores.${SyncColumns.COLUMN_DELETED_AT} IS NULL
                WHERE participants.${SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
            """.trimIndent(),
            arrayOf(playerId.toString()),
        ).use { cursor ->
            cursor.moveToFirst()
            SkatPlayerStatistics(
                listsPlayed = listsPlayed,
                roundsPlayed = cursor.intAt(0),
                soloRoundsPlayed = cursor.intAt(1),
                soloRoundsWon = cursor.intAt(2),
                roundsWonAsOpponent = cursor.intAt(3),
            )
        }
    }

    // --- Doppelkopf -----------------------------------------------------------------------

    private fun readDoppelkopfStatistics(
        db: SQLiteDatabase,
        playerId: UUID,
    ): DoppelkopfPlayerStatistics
    {
        val listsPlayed = countLists(
            db,
            playerId,
            DoppelkopfParticipantsTable.TABLE_NAME,
            DoppelkopfGamesTable.TABLE_NAME,
        )

        return db.rawQuery(
            """
                SELECT
                    COUNT(*),
                    ${countIf(PLAYER_WON)},
                    ${countIf(PLAYER_PLAYED_SOLO)},
                    ${countIf("$PLAYER_PLAYED_SOLO AND $PLAYER_WON")},
                    ${countIf(PLAYER_WAS_RE)},
                    ${countIf("$PLAYER_WAS_RE AND $DOPPELKOPF_RE_WON")}
                FROM ${DoppelkopfParticipantsTable.TABLE_NAME} participants
                INNER JOIN ${DoppelkopfGamesTable.TABLE_NAME} games
                    ON games.${DoppelkopfGamesTable.COLUMN_ID} = participants.${DoppelkopfParticipantsTable.COLUMN_GAME_ID}
                    AND games.${SyncColumns.COLUMN_DELETED_AT} IS NULL
                INNER JOIN ${DoppelkopfScoresTable.TABLE_NAME} scores
                    ON scores.${DoppelkopfScoresTable.COLUMN_GAME_ID} = participants.${DoppelkopfParticipantsTable.COLUMN_GAME_ID}
                    AND scores.${SyncColumns.COLUMN_DELETED_AT} IS NULL
                WHERE participants.${DoppelkopfParticipantsTable.COLUMN_PLAYER_ID} = ?
            """.trimIndent(),
            arrayOf(playerId.toString()),
        ).use { cursor ->
            cursor.moveToFirst()
            DoppelkopfPlayerStatistics(
                listsPlayed = listsPlayed,
                roundsPlayed = cursor.intAt(0),
                roundsWon = cursor.intAt(1),
                soloRoundsPlayed = cursor.intAt(2),
                soloRoundsWon = cursor.intAt(3),
                reRoundsPlayed = cursor.intAt(4),
                reRoundsWon = cursor.intAt(5),
            )
        }
    }

    /** Lists still there that this player sat in. */
    private fun countLists(
        db: SQLiteDatabase,
        playerId: UUID,
        participantsTable: String,
        gamesTable: String,
    ): Int = db.rawQuery(
        """
            SELECT COUNT(*)
            FROM $participantsTable participants
            INNER JOIN $gamesTable games ON games.id = participants.game_id
            WHERE participants.player_id = ?
            AND games.${SyncColumns.COLUMN_DELETED_AT} IS NULL
        """.trimIndent(),
        arrayOf(playerId.toString()),
    ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    /** SUM over no rows is NULL, not 0. */
    private fun Cursor.intAt(index: Int) = if (isNull(index)) 0 else getInt(index)

    private companion object
    {
        fun countIf(condition: String) = "SUM(CASE WHEN $condition THEN 1 ELSE 0 END)"

        private const val SKAT_SEAT = "participants.${SkatParticipantsTable.COLUMN_ID}"
        private const val SKAT_DECLARER = "scores.${SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID}"
        private const val SKAT_RESULT = "scores.${SkatScoresTable.COLUMN_WON_OR_LOST}"
        private const val SKAT_TYPE = "scores.${SkatScoresTable.COLUMN_SCORE_TYPE}"

        /**
         * The declarer column holds a participant id, so it is compared against the seat this
         * player held in that list - not against a table position.
         */
        const val PLAYER_DECLARED = "$SKAT_DECLARER = $SKAT_SEAT"

        /** Passed rounds have no declarer at all, so they are not defended either. */
        const val PLAYER_DEFENDED = "$SKAT_DECLARER IS NOT NULL AND $SKAT_DECLARER != $SKAT_SEAT"

        val SKAT_DECLARER_WON = "$SKAT_RESULT = '${WonOrLost.WON.name}'"

        /**
         * An overbid round records no result, because it is lost by definition; counting it
         * here keeps a defender's record honest.
         */
        val SKAT_DECLARER_LOST = "($SKAT_RESULT = '${WonOrLost.LOST.name}'" +
                " OR $SKAT_TYPE = '${SkatScoreType.OVERBID.name}')"

        private const val DK_SEAT = "participants.${DoppelkopfParticipantsTable.COLUMN_ID}"
        private const val DK_WINNER = "scores.${DoppelkopfScoresTable.COLUMN_WINNER}"
        private const val DK_SOLOIST = "scores.${DoppelkopfScoresTable.COLUMN_SOLOIST_ID}"
        private const val DK_RE_1 = "scores.${DoppelkopfScoresTable.COLUMN_RE_PARTICIPANT_1}"
        private const val DK_RE_2 = "scores.${DoppelkopfScoresTable.COLUMN_RE_PARTICIPANT_2}"
        private const val DK_TYPE = "scores.${DoppelkopfScoresTable.COLUMN_SCORE_TYPE}"

        /** The soloist is Re on their own; in a normal round Re is the pair of queens. */
        val PLAYER_WAS_RE =
            "(($DK_TYPE = '${DoppelkopfScoreType.SOLO.name}' AND $DK_SOLOIST = $DK_SEAT)" +
                    " OR ($DK_TYPE = '${DoppelkopfScoreType.NORMAL.name}'" +
                    " AND ($DK_RE_1 = $DK_SEAT OR $DK_RE_2 = $DK_SEAT)))"

        val DOPPELKOPF_RE_WON = "$DK_WINNER = '${DoppelkopfParty.RE.name}'"

        const val PLAYER_PLAYED_SOLO = "$DK_SOLOIST = $DK_SEAT"

        /**
         * The player ends up on the winning side: either they were Re and Re won, or they
         * were not Re and Kontra won.
         */
        val PLAYER_WON =
            "(($PLAYER_WAS_RE AND $DK_WINNER = '${DoppelkopfParty.RE.name}')" +
                    " OR (NOT $PLAYER_WAS_RE" +
                    " AND $DK_WINNER = '${DoppelkopfParty.KONTRA.name}'))"
    }
}
