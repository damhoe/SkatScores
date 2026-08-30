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
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_doppelkopf_player
ON doppelkopf_participants(game_id, player_id)
WHERE player_id IS NOT NULL;
