package com.damhoe.skatscores.persistence

import android.database.sqlite.SQLiteDatabase
import com.damhoe.skatscores.persistence.Schema.NOW

/**
 * One step per version, applied in order by [DatabaseHelper.onUpgrade], so a database of any
 * earlier version walks the same path to the current one. A step is only ever added, never
 * edited once released: a device may be sitting on any version in between.
 *
 * Every step runs inside the upgrade's transaction and throws on any failure, which rolls the
 * whole upgrade back and leaves the version where it was - the next launch tries again,
 * rather than carrying on with half a schema.
 */
object Migrations
{
    /** Keyed by the version a step produces. */
    val steps: Map<Int, (SQLiteDatabase) -> Unit> = mapOf(
        2 to ::toVersion2,
        3 to ::toVersion3,
    )

    fun migrate(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        for (version in oldVersion + 1..newVersion)
        {
            val step = steps[version]
                ?: throw IllegalStateException("No migration to database version $version")
            step(db)
        }
    }

    /**
     * Version 2 added Doppelkopf. The tables are created in the shape they had then; version 3
     * rebuilds them like every other table.
     */
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
     * Version 3 rebuilds every table:
     *
     * - The foreign keys a version 1 database declared point at a table called `games`, which
     *   never existed. Nothing noticed because enforcement was off; with it on, every insert
     *   into those tables would fail. A table's constraints cannot be changed in place.
     * - Each row gains `updated_at`, `version` and, where rows can be deleted, `deleted_at`.
     * - Rounds are ordered by `seq` instead of a dense `round` number. Existing rounds keep
     *   their order: their seq is derived from the old number and sorts before anything
     *   written from now on.
     *
     * Rows that could never be read - a seat or round whose list is gone, a round whose
     * declarer seat is gone - are left behind rather than carried into tables that now
     * enforce their references. A seat linked to a player that no longer exists is kept and
     * unlinked, which is what deleting the player should have done in the first place.
     *
     * Foreign keys are off while this runs ([DatabaseHelper] only switches them on once the
     * database is open), so dropping an old table does not cascade into the new ones.
     */
    private fun toVersion3(db: SQLiteDatabase)
    {
        // 1. The new tables, next to the old ones.
        db.execSQL(Schema.createPlayers("players_new"))
        db.execSQL(Schema.createSkatGames("skat_games_new"))
        db.execSQL(Schema.createSkatParticipants("skat_participants_new"))
        db.execSQL(Schema.createSkatScores("skat_scores_new"))
        db.execSQL(Schema.createDoppelkopfGames("doppelkopf_games_new"))
        db.execSQL(Schema.createDoppelkopfParticipants("doppelkopf_participants_new"))
        db.execSQL(Schema.createDoppelkopfScores("doppelkopf_scores_new"))

        // 2. Copy. Old timestamps are kept where they are well-formed; the players trigger
        // wrote updated_at as "…12.12.345Z", so that one is restamped.
        db.execSQL(
            """
            INSERT INTO players_new (id, name, created_at, updated_at)
            SELECT id, name, COALESCE(created_at, $NOW), $NOW
            FROM players"""
        )

        // player_count was written as the enum's ordinal into a text column.
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

        // 3. Swap. Dropping a table drops its triggers and indexes with it - the updated_at
        // triggers are not wanted any more; the app stamps rows itself.
        val tables = listOf(
            "players",
            "skat_games",
            "skat_participants",
            "skat_scores",
            "doppelkopf_games",
            "doppelkopf_participants",
            "doppelkopf_scores",
        )
        tables.forEach { db.execSQL("DROP TABLE $it") }
        tables.forEach { db.execSQL("ALTER TABLE ${it}_new RENAME TO $it") }

        Schema.INDEXES.forEach(db::execSQL)

        db.execSQL(Schema.createSyncMeta())
        Schema.seedDeviceId(db)
    }

    /**
     * The seq an existing round gets from its old number: time zero, the number as the
     * counter. Sorts in the old order, and before every seq [RoundSequence] hands out.
     */
    private fun legacySeq(roundColumn: String) =
        "printf('%013d-%04d-%s', 0, $roundColumn, '${RoundSequence.LEGACY_DEVICE}')"
}
