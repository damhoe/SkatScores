CREATE TABLE IF NOT EXISTS doppelkopf_scores (
    id VARCHAR(36) PRIMARY KEY,

    game_id VARCHAR(36) NOT NULL,
    round INT NOT NULL,

    score_type VARCHAR(20) NOT NULL,
    winner VARCHAR(10) NOT NULL,

    -- Normal round: the two seats that played as Re.
    re_participant_1 VARCHAR(36),
    re_participant_2 VARCHAR(36),

    -- Solo: the seat that played alone and what it played.
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
);
