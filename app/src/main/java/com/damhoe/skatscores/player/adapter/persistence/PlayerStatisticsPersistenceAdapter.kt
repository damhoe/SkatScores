package com.damhoe.skatscores.player.adapter.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.doppelkopf.adapter.persistence.scores.DoppelkopfScoreType
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreType
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfScoresTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.run
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
 * participant rows carrying this player's id and joins that game's scores onto them. Skat and
 * Doppelkopf never share a table, so the two halves are counted separately and only added up
 * in [PlayerStatistics].
 */
class PlayerStatisticsPersistenceAdapter @Inject constructor(
    private val dbHelper: DatabaseHelper,
)
{
    private val readableDatabase = dbHelper.readableDatabase

    fun getPlayerStatistics(playerId: UUID): Result<PlayerStatistics>
    {
        return readableDatabase.run {
            Result.success(
                PlayerStatistics(
                    skat = readSkatStatistics(this, playerId),
                    doppelkopf = readDoppelkopfStatistics(this, playerId),
                )
            )
        }
    }

    /**
     * Lists the player appears in, per game. The two are returned apart rather than added up:
     * the screens that show this name the games, and a caller that only wants the total can
     * ask [ListCounts] for it.
     */
    fun getListCounts(playerId: UUID): Result<ListCounts>
    {
        return readableDatabase.run {
            Result.success(
                ListCounts(
                    skat = countSkatLists(this, playerId),
                    doppelkopf = countDoppelkopfLists(this, playerId),
                )
            )
        }
    }

    private fun count(db: SQLiteDatabase, query: String, args: Array<String>): Int
    {
        db.rawQuery(query, args).use { cursor ->
            if (cursor.moveToFirst()) return cursor.getInt(0)
        }
        return 0
    }

    // --- Skat -----------------------------------------------------------------------------

    private fun readSkatStatistics(db: SQLiteDatabase, playerId: UUID) = SkatPlayerStatistics(
        listsPlayed = countSkatLists(db, playerId),
        roundsPlayed = countSkatRounds(db, playerId, extraCondition = ""),
        soloRoundsPlayed = countSkatRounds(db, playerId, extraCondition = AND_PLAYER_DECLARED),
        soloRoundsWon = countSkatRounds(
            db,
            playerId,
            extraCondition = "$AND_PLAYER_DECLARED AND $SKAT_DECLARER_WON",
        ),
        roundsWonAsOpponent = countSkatRounds(
            db,
            playerId,
            extraCondition = "$AND_PLAYER_DEFENDED AND $SKAT_DECLARER_LOST",
        ),
    )

    private fun countSkatLists(db: SQLiteDatabase, playerId: UUID) = count(
        db,
        """
            SELECT COUNT(*)
            FROM ${SkatParticipantsTable.TABLE_NAME} participants
            WHERE participants.${SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
        """.trimIndent(),
        arrayOf(playerId.toString()),
    )

    /**
     * Rounds of the lists this player sat in, narrowed by [extraCondition].
     *
     * The join is on the participant row of this player, so `participants.id` is the seat the
     * player held in that list - which is what a score's declarer column points at.
     */
    private fun countSkatRounds(
        db: SQLiteDatabase,
        playerId: UUID,
        extraCondition: String,
    ) = count(
        db,
        """
            SELECT COUNT(*)
            FROM ${SkatScoresTable.TABLE_NAME} scores
            INNER JOIN ${SkatParticipantsTable.TABLE_NAME} participants
            ON scores.${SkatScoresTable.COLUMN_GAME_ID} = participants.${SkatParticipantsTable.COLUMN_GAME_ID}
            WHERE participants.${SkatParticipantsTable.COLUMN_PLAYER_ID} = ?
            $extraCondition
        """.trimIndent(),
        arrayOf(playerId.toString()),
    )

    // --- Doppelkopf -----------------------------------------------------------------------

    private fun readDoppelkopfStatistics(db: SQLiteDatabase, playerId: UUID) =
        DoppelkopfPlayerStatistics(
            listsPlayed = countDoppelkopfLists(db, playerId),
            roundsPlayed = countDoppelkopfRounds(db, playerId, extraCondition = ""),
            roundsWon = countDoppelkopfRounds(db, playerId, extraCondition = AND_PLAYER_WON),
            soloRoundsPlayed = countDoppelkopfRounds(
                db,
                playerId,
                extraCondition = AND_PLAYER_PLAYED_SOLO,
            ),
            soloRoundsWon = countDoppelkopfRounds(
                db,
                playerId,
                extraCondition = "$AND_PLAYER_PLAYED_SOLO AND $AND_PLAYER_WON_INNER",
            ),
            reRoundsPlayed = countDoppelkopfRounds(
                db,
                playerId,
                extraCondition = AND_PLAYER_WAS_RE,
            ),
            reRoundsWon = countDoppelkopfRounds(
                db,
                playerId,
                extraCondition = "$AND_PLAYER_WAS_RE AND $DOPPELKOPF_RE_WON",
            ),
        )

    private fun countDoppelkopfLists(db: SQLiteDatabase, playerId: UUID) = count(
        db,
        """
            SELECT COUNT(*)
            FROM ${DoppelkopfParticipantsTable.TABLE_NAME} participants
            WHERE participants.${DoppelkopfParticipantsTable.COLUMN_PLAYER_ID} = ?
        """.trimIndent(),
        arrayOf(playerId.toString()),
    )

    private fun countDoppelkopfRounds(
        db: SQLiteDatabase,
        playerId: UUID,
        extraCondition: String,
    ) = count(
        db,
        """
            SELECT COUNT(*)
            FROM ${DoppelkopfScoresTable.TABLE_NAME} scores
            INNER JOIN ${DoppelkopfParticipantsTable.TABLE_NAME} participants
            ON scores.${DoppelkopfScoresTable.COLUMN_GAME_ID} = participants.${DoppelkopfParticipantsTable.COLUMN_GAME_ID}
            WHERE participants.${DoppelkopfParticipantsTable.COLUMN_PLAYER_ID} = ?
            $extraCondition
        """.trimIndent(),
        arrayOf(playerId.toString()),
    )

    private companion object
    {
        private const val SKAT_SEAT = "participants.${SkatParticipantsTable.COLUMN_ID}"
        private const val SKAT_DECLARER = "scores.${SkatScoresTable.COLUMN_SKAT_PARTICIPANTS_ID}"
        private const val SKAT_RESULT = "scores.${SkatScoresTable.COLUMN_WON_OR_LOST}"
        private const val SKAT_TYPE = "scores.${SkatScoresTable.COLUMN_SCORE_TYPE}"

        /**
         * The declarer column holds a participant id, so it is compared against the seat this
         * player held in that list - not against a table position.
         */
        const val AND_PLAYER_DECLARED = "AND $SKAT_DECLARER = $SKAT_SEAT"

        /** Passed rounds have no declarer at all, so they are not defended either. */
        const val AND_PLAYER_DEFENDED =
            "AND $SKAT_DECLARER IS NOT NULL AND $SKAT_DECLARER != $SKAT_SEAT"

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
        private val PLAYER_WAS_RE =
            "(($DK_TYPE = '${DoppelkopfScoreType.SOLO.name}' AND $DK_SOLOIST = $DK_SEAT)" +
                    " OR ($DK_TYPE = '${DoppelkopfScoreType.NORMAL.name}'" +
                    " AND ($DK_RE_1 = $DK_SEAT OR $DK_RE_2 = $DK_SEAT)))"

        val DOPPELKOPF_RE_WON = "$DK_WINNER = '${DoppelkopfParty.RE.name}'"

        val AND_PLAYER_WAS_RE = "AND $PLAYER_WAS_RE"

        const val AND_PLAYER_PLAYED_SOLO = "AND $DK_SOLOIST = $DK_SEAT"

        /**
         * The player ends up on the winning side: either they were Re and Re won, or they
         * were not Re and Kontra won.
         */
        val AND_PLAYER_WON_INNER =
            "(($PLAYER_WAS_RE AND $DK_WINNER = '${DoppelkopfParty.RE.name}')" +
                    " OR (NOT $PLAYER_WAS_RE" +
                    " AND $DK_WINNER = '${DoppelkopfParty.KONTRA.name}'))"

        val AND_PLAYER_WON = "AND $AND_PLAYER_WON_INNER"
    }
}
