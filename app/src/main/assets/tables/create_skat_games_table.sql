CREATE TABLE IF NOT EXISTS skat_games (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(30) NOT NULL,
    player_count VARCHAR(10) NOT NULL,
    played_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    updated_at TEXT DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    round_count INTEGER NOT NULL,
    scoring_mode VARCHAR(10) NOT NULL
);

CREATE TRIGGER IF NOT EXISTS update_skat_games_updated_at
AFTER UPDATE ON skat_games
FOR EACH ROW
BEGIN
    UPDATE skat_games SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE id = NEW.id;
END;