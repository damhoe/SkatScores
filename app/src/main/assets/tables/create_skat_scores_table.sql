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

    FOREIGN KEY (game_id) REFERENCES skat_games(id) ON DELETE CASCADE,
    FOREIGN KEY (skat_participant_id) REFERENCES skat_participants(id) ON DELETE CASCADE,

    CONSTRAINT unique_game_round UNIQUE (game_id, round)
);