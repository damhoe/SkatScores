CREATE TABLE IF NOT EXISTS players (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE,

    created_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    updated_at TIMESTAMP DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TRIGGER IF NOT EXISTS update_players_updated_at
AFTER UPDATE ON players
FOR EACH ROW
BEGIN
    UPDATE players SET updated_at = strftime('%Y-%m-%dT%H:%M:%S.%fZ', 'now') WHERE id = NEW.id;
END;