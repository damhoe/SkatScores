package com.damhoe.skatscores.persistence

/**
 * The databases earlier builds created, statement for statement, so the migrations can be run
 * against the real thing. Version 1 differs from 2 in having no Doppelkopf tables and in its
 * foreign keys naming a `games` table that never existed.
 */
object LegacySchemas
{
    private fun skat(gamesTable: String) = listOf(
        """
        CREATE TABLE IF NOT EXISTS players (
            id VARCHAR(36) PRIMARY KEY,
            name VARCHAR(40) NOT NULL UNIQUE,
            created_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            updated_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
        )""",
        """
        CREATE TRIGGER IF NOT EXISTS update_players_updated_at
        AFTER UPDATE ON players
        FOR EACH ROW
        BEGIN
            UPDATE players SET updated_at = strftime('%Y-%m-%dT%H:%M:%S.%fZ', 'now') WHERE id = NEW.id;
        END""",
        """
        CREATE TABLE IF NOT EXISTS skat_games (
            id VARCHAR(36) PRIMARY KEY,
            title VARCHAR(30) NOT NULL,
            player_count VARCHAR(10) NOT NULL,
            played_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            updated_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            round_count INTEGER NOT NULL,
            scoring_mode VARCHAR(10) NOT NULL
        )""",
        """
        CREATE TRIGGER IF NOT EXISTS update_skat_games_updated_at
        AFTER UPDATE ON skat_games
        FOR EACH ROW
        BEGIN
            UPDATE skat_games SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE id = NEW.id;
        END""",
        """
        CREATE TABLE IF NOT EXISTS skat_participants (
            id VARCHAR(36) PRIMARY KEY,
            game_id VARCHAR(36) NOT NULL,
            player_id VARCHAR(36) NULL,
            table_position VARCHAR(20),
            name VARCHAR(255) NOT NULL,
            FOREIGN KEY (game_id) REFERENCES $gamesTable(id) ON DELETE CASCADE,
            FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE SET NULL,
            CONSTRAINT unique_table_position UNIQUE (game_id, table_position),
            CONSTRAINT unique_participant UNIQUE (game_id, name)
        )""",
        """
        CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_player
        ON skat_participants(game_id, player_id)
        WHERE player_id IS NOT NULL""",
        """
        CREATE TABLE IF NOT EXISTS skat_scores (
            id VARCHAR(36) PRIMARY KEY,
            game_id VARCHAR(36) NOT NULL,
            skat_participant_id VARCHAR(36) NULL,
            round INT NOT NULL,
            score_type VARCHAR(20) NOT NULL,
            won_or_lost VARCHAR(10),
            overbid_suit VARCHAR(10),
            overbid_bid_value INT,
            null_options VARCHAR(20),
            grand_or_suit_suit VARCHAR(10),
            grand_or_suit_spitzen_value INT,
            grand_or_suit_options VARCHAR(50),
            created_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            updated_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            FOREIGN KEY (game_id) REFERENCES $gamesTable(id) ON DELETE CASCADE,
            FOREIGN KEY (skat_participant_id) REFERENCES skat_participants(id) ON DELETE CASCADE,
            CONSTRAINT unique_game_round UNIQUE (game_id, round)
        )""",
    )

    private val doppelkopf = listOf(
        """
        CREATE TABLE IF NOT EXISTS doppelkopf_games (
            id VARCHAR(36) PRIMARY KEY,
            title VARCHAR(30) NOT NULL,
            played_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            updated_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            round_count INTEGER NOT NULL,
            scoring_mode VARCHAR(10) NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS doppelkopf_participants (
            id VARCHAR(36) PRIMARY KEY,
            game_id VARCHAR(36) NOT NULL,
            player_id VARCHAR(36) NULL,
            seat INTEGER NOT NULL,
            name VARCHAR(255) NOT NULL,
            FOREIGN KEY (game_id) REFERENCES doppelkopf_games(id) ON DELETE CASCADE,
            FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE SET NULL,
            CONSTRAINT unique_doppelkopf_seat UNIQUE (game_id, seat),
            CONSTRAINT unique_doppelkopf_participant UNIQUE (game_id, name)
        )""",
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
            created_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            updated_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
            FOREIGN KEY (game_id) REFERENCES doppelkopf_games(id) ON DELETE CASCADE,
            CONSTRAINT unique_doppelkopf_game_round UNIQUE (game_id, round)
        )""",
    )

    val VERSION_1 = skat(gamesTable = "games")

    val VERSION_2 = skat(gamesTable = "skat_games") + doppelkopf
}
