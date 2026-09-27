package com.damhoe.skatscores.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.persistence.Schema.NOW

/**
 * Released steps must never be edited: a device may be on any earlier version. A failing step
 * throws, which rolls back the whole upgrade.
 */
object Migrations
{
    private val stepsToVersion: Map<Int, (SQLiteDatabase) -> Unit> = mapOf(
        2 to ::toVersion2,
        3 to ::toVersion3,
    )

    fun migrate(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        for (version in oldVersion + 1..newVersion)
        {
            val step = stepsToVersion[version]
                ?: throw IllegalStateException("No migration to database version $version")
            step(db)
        }
    }

    /** Doppelkopf tables in their version-2 shape; version 3 rebuilds them. */
    private fun toVersion2(db: SQLiteDatabase)
    {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS doppelkopf_games (
                id VARCHAR(36) PRIMARY KEY,
                title VARCHAR(30) NOT NULL,
                played_at TEXT DEFAULT ($NOW),
                updated_at TEXT DEFAULT ($NOW),
                round_count INTEGER NOT NULL,
                scoring_mode VARCHAR(10) NOT NULL
            )"""
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS doppelkopf_participants (
                id VARCHAR(36) PRIMARY KEY,
                game_id VARCHAR(36) NOT NULL,
                player_id VARCHAR(36) NULL,
                seat INTEGER NOT NULL,
                name VARCHAR(255) NOT NULL,
                UNIQUE (game_id, seat),
                UNIQUE (game_id, name)
            )"""
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS doppelkopf_scores (
                id VARCHAR(36) PRIMARY KEY,
                game_id VARCHAR(36) NOT NULL,
                round INT NOT NULL,
                score_type VARCHAR(20) NOT NULL,
                winner VARCHAR(10) NOT NULL,
                re_participant_1 VARCHAR(36),
                re_participant_2 VARCHAR(36),
                soloist_id VARCHAR(36),
                solo_kind VARCHAR(20),
                win_level VARCHAR(10) NOT NULL,
                absage VARCHAR(10),
                re_announced INTEGER NOT NULL DEFAULT 0,
                kontra_announced INTEGER NOT NULL DEFAULT 0,
                extra_points_re INTEGER NOT NULL DEFAULT 0,
                extra_points_kontra INTEGER NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT ($NOW),
                updated_at TIMESTAMP DEFAULT ($NOW),
                UNIQUE (game_id, round)
            )"""
        )
    }

    /**
     * Rebuilds every table: SQLite cannot change constraints in place, and version-1 foreign
     * keys reference a nonexistent `games` table. Adds the sync columns and replaces `round`
     * with `seq`. Rows whose parent is missing are dropped; links to missing players are
     * cleared.
     *
     * Runs with foreign keys off (see [DatabaseHelper.onOpen]), so dropping the old tables
     * does not cascade.
     */
    private fun toVersion3(db: SQLiteDatabase)
    {
        createTablesNextToOld(db)
        copyRows(db)
        replaceOldTables(db)

        Schema.INDEXES.forEach(db::execSQL)

        db.execSQL(Schema.createSyncMeta())
        Schema.seedDeviceId(db)
    }

    private val rebuiltTables = listOf(
        "players",
        "skat_games",
        "skat_participants",
        "skat_scores",
        "doppelkopf_games",
        "doppelkopf_participants",
        "doppelkopf_scores",
    )

    private fun createTablesNextToOld(db: SQLiteDatabase)
    {
        db.execSQL(Schema.createPlayers("players_new"))
        db.execSQL(Schema.createSkatGames("skat_games_new"))
        db.execSQL(Schema.createSkatParticipants("skat_participants_new"))
        db.execSQL(Schema.createSkatScores("skat_scores_new"))
        db.execSQL(Schema.createDoppelkopfGames("doppelkopf_games_new"))
        db.execSQL(Schema.createDoppelkopfParticipants("doppelkopf_participants_new"))
        db.execSQL(Schema.createDoppelkopfScores("doppelkopf_scores_new"))
    }

    private fun copyRows(db: SQLiteDatabase)
    {
        // The old players trigger wrote a malformed updated_at, so it is restamped.
        db.execSQL(
            """
            INSERT INTO players_new (id, name, created_at, updated_at)
            SELECT id, name, COALESCE(created_at, $NOW), $NOW
            FROM players"""
        )

        // player_count held the enum ordinal.
        db.execSQL(
            """
            INSERT INTO skat_games_new
                (id, title, player_count, round_count, scoring_mode, played_at, updated_at)
            SELECT id, title,
                CASE CAST(player_count AS TEXT)
                    WHEN '0' THEN 'THREE_PLAYERS'
                    WHEN '1' THEN 'FOUR_PLAYERS'
                    ELSE player_count
                END,
                round_count, scoring_mode,
                COALESCE(played_at, $NOW), COALESCE(updated_at, $NOW)
            FROM skat_games"""
        )

        db.execSQL(
            """
            INSERT INTO skat_participants_new (id, game_id, player_id, table_position, name)
            SELECT id, game_id,
                CASE WHEN player_id IN (SELECT id FROM players) THEN player_id END,
                table_position, name
            FROM skat_participants
            WHERE game_id IN (SELECT id FROM skat_games)"""
        )

        db.execSQL(
            """
            INSERT INTO skat_scores_new (
                id, game_id, skat_participant_id, seq, score_type, won_or_lost,
                overbid_suit, overbid_bid_value, null_options,
                grand_or_suit_suit, grand_or_suit_spitzen_value, grand_or_suit_options,
                created_at, updated_at)
            SELECT id, game_id, skat_participant_id, ${legacySeq("round")}, score_type,
                won_or_lost, overbid_suit, overbid_bid_value, null_options,
                grand_or_suit_suit, grand_or_suit_spitzen_value, grand_or_suit_options,
                COALESCE(created_at, $NOW), COALESCE(updated_at, $NOW)
            FROM skat_scores
            WHERE game_id IN (SELECT id FROM skat_games)
            AND (skat_participant_id IS NULL
                OR skat_participant_id IN (SELECT id FROM skat_participants_new))"""
        )

        db.execSQL(
            """
            INSERT INTO doppelkopf_games_new
                (id, title, round_count, scoring_mode, played_at, updated_at)
            SELECT id, title, round_count, scoring_mode,
                COALESCE(played_at, $NOW), COALESCE(updated_at, $NOW)
            FROM doppelkopf_games"""
        )

        db.execSQL(
            """
            INSERT INTO doppelkopf_participants_new (id, game_id, player_id, seat, name)
            SELECT id, game_id,
                CASE WHEN player_id IN (SELECT id FROM players) THEN player_id END,
                seat, name
            FROM doppelkopf_participants
            WHERE game_id IN (SELECT id FROM doppelkopf_games)"""
        )

        db.execSQL(
            """
            INSERT INTO doppelkopf_scores_new (
                id, game_id, seq, score_type, winner,
                re_participant_1, re_participant_2, soloist_id, solo_kind,
                win_level, absage, re_announced, kontra_announced,
                extra_points_re, extra_points_kontra, created_at, updated_at)
            SELECT id, game_id, ${legacySeq("round")}, score_type, winner,
                re_participant_1, re_participant_2, soloist_id, solo_kind,
                win_level, absage, re_announced, kontra_announced,
                extra_points_re, extra_points_kontra,
                COALESCE(created_at, $NOW), COALESCE(updated_at, $NOW)
            FROM doppelkopf_scores
            WHERE game_id IN (SELECT id FROM doppelkopf_games)"""
        )
    }

    /** Dropping the old tables also drops their updated_at triggers, which are not wanted. */
    private fun replaceOldTables(db: SQLiteDatabase)
    {
        rebuiltTables.forEach { db.execSQL("DROP TABLE $it") }
        rebuiltTables.forEach { db.execSQL("ALTER TABLE ${it}_new RENAME TO $it") }
    }

    /** Time zero with the old round number as counter: keeps the old order, sorts first. */
    private fun legacySeq(roundColumn: String) =
        "printf('%013d-%04d-%s', 0, $roundColumn, '${RoundSequence.LEGACY_DEVICE}')"
}
