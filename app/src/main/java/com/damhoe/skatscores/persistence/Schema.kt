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
 * The current shape of the database, and the only place it is written down.
 *
 * A fresh install runs [create]; an existing one reaches the same shape through [Migrations].
 * Each table is a function of its name so a migration can build the new version next to the
 * old one (`skat_games_new`), copy the rows across and swap them - SQLite cannot add a
 * constraint or drop a column in place.
 *
 * Every foreign key names the final table, never a `_new` one: the migration renames the new
 * tables into place, and a reference to a temporary name would be left dangling.
 */
object Schema
{
    const val VERSION = 3

    /**
     * The one timestamp format in the database: ISO-8601 UTC with milliseconds, which
     * [java.time.Instant.parse] reads and which sorts as text in time order.
     */
    const val NOW = "strftime('%Y-%m-%dT%H:%M:%fZ', 'now')"

    private val TIMESTAMP_FORMAT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

    /** [instant] the way [NOW] writes it, so the two compare as text. */
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

    /** Seats live and die with their list, so they carry no tombstone of their own. */
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

    /**
     * Rounds are ordered by `seq` (see [RoundSequence]) and not numbered: a dense round number
     * had to be rewritten for every later round whenever one was removed, and two devices
     * adding a round at the same time would both have claimed the same number.
     */
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

    /** The seat columns hold participant ids but are not foreign keys, as before. */
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

    /**
     * The indexes of the current version. Created after the tables are in place under their
     * final names, since an index belongs to the table it was created on.
     *
     * - A player name is unique among the players still there; a deleted one frees its name.
     * - Every lookup by `player_id` - statistics, list counts, unlinking a deleted player -
     *   used to scan the whole participants table, because the only index started with
     *   `game_id`.
     */
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

    /** A fresh database at [VERSION]. */
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

    /**
     * Names this installation for [RoundSequence]. Generated once and kept; a device that
     * restores a backup of another carries its id along, which only costs a tie-breaker.
     */
    fun seedDeviceId(db: SQLiteDatabase)
    {
        db.execSQL(
            "INSERT OR IGNORE INTO ${SyncMetaTable.TABLE_NAME}" +
                    " (${SyncMetaTable.COLUMN_KEY}, ${SyncMetaTable.COLUMN_VALUE}) VALUES (?, ?)",
            arrayOf(SyncMetaTable.KEY_DEVICE_ID, UUID.randomUUID().toString().replace("-", ""))
        )
    }
}
