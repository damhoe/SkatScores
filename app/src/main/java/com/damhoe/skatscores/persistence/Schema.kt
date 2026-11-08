package com.damhoe.skatscores.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.DoppelkopfScoresTable
import com.damhoe.skatscores.persistence.DatabaseConstants.PlayersTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatParticipantsTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatScoresTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncMetaTable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * The current schema; [Migrations] brings older databases here. Tables take their name as a
 * parameter so a migration can build `<table>_new` beside the old one. Foreign keys always name
 * the final table, since the `_new` tables are renamed into place.
 */
object Schema
{
    const val VERSION = 3

    /** ISO-8601 UTC with milliseconds: parseable by [Instant.parse], sorts as text. */
    const val NOW = "strftime('%Y-%m-%dT%H:%M:%fZ', 'now')"

    private val TIMESTAMP_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

    /** Same format as [NOW], so the two compare as text. */
    fun timestamp(instant: Instant): String = TIMESTAMP_FORMAT.format(instant)

    private const val SYNC_COLUMNS = """
        updated_at TEXT NOT NULL DEFAULT ($NOW),
        version INTEGER NOT NULL DEFAULT 1"""

    fun createPlayers(table: String = PlayersTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            name TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT ($NOW),
            $SYNC_COLUMNS,
            deleted_at TEXT
        )"""

    fun createSkatGames(table: String = SkatGamesTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            player_count TEXT NOT NULL,
            round_count INTEGER NOT NULL,
            scoring_mode TEXT NOT NULL,
            played_at TEXT NOT NULL DEFAULT ($NOW),
            $SYNC_COLUMNS,
            deleted_at TEXT
        )"""

    /** No `deleted_at`: seats are hidden with their list. */
    fun createSkatParticipants(table: String = SkatParticipantsTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            game_id TEXT NOT NULL REFERENCES ${SkatGamesTable.TABLE_NAME}(id) ON DELETE CASCADE,
            player_id TEXT REFERENCES ${PlayersTable.TABLE_NAME}(id) ON DELETE SET NULL,
            table_position TEXT NOT NULL,
            name TEXT NOT NULL,
            $SYNC_COLUMNS,
            UNIQUE (game_id, table_position),
            UNIQUE (game_id, name)
        )"""

    /** Rounds are ordered by `seq` (see [RoundSequence]); the round number is the position. */
    fun createSkatScores(table: String = SkatScoresTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            game_id TEXT NOT NULL REFERENCES ${SkatGamesTable.TABLE_NAME}(id) ON DELETE CASCADE,
            skat_participant_id TEXT
                REFERENCES ${SkatParticipantsTable.TABLE_NAME}(id) ON DELETE CASCADE,
            seq TEXT NOT NULL,
            score_type TEXT NOT NULL,
            won_or_lost TEXT,
            overbid_suit TEXT,
            overbid_bid_value INTEGER,
            null_options TEXT,
            grand_or_suit_suit TEXT,
            grand_or_suit_spitzen_value INTEGER,
            grand_or_suit_options TEXT,
            created_at TEXT NOT NULL DEFAULT ($NOW),
            $SYNC_COLUMNS,
            deleted_at TEXT,
            UNIQUE (game_id, seq)
        )"""

    fun createDoppelkopfGames(table: String = DoppelkopfGamesTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            round_count INTEGER NOT NULL,
            scoring_mode TEXT NOT NULL,
            played_at TEXT NOT NULL DEFAULT ($NOW),
            $SYNC_COLUMNS,
            deleted_at TEXT
        )"""

    fun createDoppelkopfParticipants(table: String = DoppelkopfParticipantsTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            game_id TEXT NOT NULL
                REFERENCES ${DoppelkopfGamesTable.TABLE_NAME}(id) ON DELETE CASCADE,
            player_id TEXT REFERENCES ${PlayersTable.TABLE_NAME}(id) ON DELETE SET NULL,
            seat INTEGER NOT NULL,
            name TEXT NOT NULL,
            $SYNC_COLUMNS,
            UNIQUE (game_id, seat),
            UNIQUE (game_id, name)
        )"""

    /** The Re and soloist columns hold participant ids but are not foreign keys. */
    fun createDoppelkopfScores(table: String = DoppelkopfScoresTable.TABLE_NAME) = """
        CREATE TABLE $table (
            id TEXT PRIMARY KEY NOT NULL,
            game_id TEXT NOT NULL
                REFERENCES ${DoppelkopfGamesTable.TABLE_NAME}(id) ON DELETE CASCADE,
            seq TEXT NOT NULL,
            score_type TEXT NOT NULL,
            winner TEXT NOT NULL,
            re_participant_1 TEXT,
            re_participant_2 TEXT,
            soloist_id TEXT,
            solo_kind TEXT,
            win_level TEXT NOT NULL,
            absage TEXT,
            re_announced INTEGER NOT NULL DEFAULT 0,
            kontra_announced INTEGER NOT NULL DEFAULT 0,
            extra_points_re INTEGER NOT NULL DEFAULT 0,
            extra_points_kontra INTEGER NOT NULL DEFAULT 0,
            created_at TEXT NOT NULL DEFAULT ($NOW),
            $SYNC_COLUMNS,
            deleted_at TEXT,
            UNIQUE (game_id, seq)
        )"""

    fun createSyncMeta() = """
        CREATE TABLE ${SyncMetaTable.TABLE_NAME} (
            key TEXT PRIMARY KEY NOT NULL,
            value TEXT NOT NULL
        )"""

    /** Created once the tables have their final names. Deleted players free their name. */
    val INDEXES = listOf(
        """CREATE UNIQUE INDEX idx_players_live_name
            ON players(name) WHERE deleted_at IS NULL""",
        "CREATE INDEX idx_skat_games_played_at ON skat_games(played_at)",
        """CREATE UNIQUE INDEX idx_skat_participants_game_player
            ON skat_participants(game_id, player_id) WHERE player_id IS NOT NULL""",
        "CREATE INDEX idx_skat_participants_player ON skat_participants(player_id)",
        "CREATE INDEX idx_skat_scores_participant ON skat_scores(skat_participant_id)",
        "CREATE INDEX idx_doppelkopf_games_played_at ON doppelkopf_games(played_at)",
        """CREATE UNIQUE INDEX idx_doppelkopf_participants_game_player
            ON doppelkopf_participants(game_id, player_id) WHERE player_id IS NOT NULL""",
        "CREATE INDEX idx_doppelkopf_participants_player ON doppelkopf_participants(player_id)",
    )

    fun create(db: SQLiteDatabase)
    {
        listOf(
            createPlayers(),
            createSkatGames(),
            createSkatParticipants(),
            createSkatScores(),
            createDoppelkopfGames(),
            createDoppelkopfParticipants(),
            createDoppelkopfScores(),
            createSyncMeta(),
        ).forEach(db::execSQL)

        INDEXES.forEach(db::execSQL)

        seedDeviceId(db)
    }

    /** Identifies this installation in [RoundSequence]; a restored backup carries it along. */
    fun seedDeviceId(db: SQLiteDatabase)
    {
        db.execSQL(
            "INSERT OR IGNORE INTO ${SyncMetaTable.TABLE_NAME}" +
                    " (${SyncMetaTable.COLUMN_KEY}, ${SyncMetaTable.COLUMN_VALUE}) VALUES (?, ?)",
            arrayOf(SyncMetaTable.KEY_DEVICE_ID, UUID.randomUUID().toString().replace("-", ""))
        )
    }
}
