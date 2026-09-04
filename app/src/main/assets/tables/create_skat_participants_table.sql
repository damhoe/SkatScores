CREATE TABLE IF NOT EXISTS skat_participants (
    id VARCHAR(36) PRIMARY KEY,
    game_id VARCHAR(36) NOT NULL,
    player_id VARCHAR(36) NULL,
    table_position VARCHAR(20),
    name VARCHAR(255) NOT NULL,

    FOREIGN KEY (game_id) REFERENCES skat_games(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE SET NULL,

    CONSTRAINT unique_table_position UNIQUE (game_id, table_position),
    CONSTRAINT unique_participant UNIQUE (game_id, name)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_player
ON skat_participants(game_id, player_id)
WHERE player_id IS NOT NULL;