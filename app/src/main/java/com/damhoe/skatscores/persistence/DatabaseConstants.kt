package com.damhoe.skatscores.persistence

object DatabaseConstants
{
    const val DATABASE_NAME = "skat_scores"

    object PlayersTable
    {
        const val TABLE_NAME: String = "players"
        const val COLUMN_ID: String = "id"
        const val COLUMN_NAME: String = "name"

        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    object SkatGamesTable
    {
        const val TABLE_NAME: String = "skat_games"
        const val COLUMN_ID: String = "id"
        const val COLUMN_TITLE: String = "title"
        const val COLUMN_PLAYER_COUNT: String = "player_count"

        const val COLUMN_ROUND_COUNT: String = "round_count"
        const val COLUMN_SCORING_MODE: String = "scoring_mode"

        const val COLUMN_PLAYED_AT = "played_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    object SkatParticipantsTable
    {
        const val TABLE_NAME: String = "skat_participants"
        const val COLUMN_ID: String = "id"
        const val COLUMN_GAME_ID: String = "game_id" // Foreign key to games.id
        const val COLUMN_PLAYER_ID: String = "player_id" // Foreign key to players.id
        const val COLUMN_TABLE_POSITION: String = "table_position"
        const val COLUMN_NAME: String = "name"
    }

    object SkatScoresTable
    {
        const val TABLE_NAME = "skat_scores"
        const val COLUMN_ID = "id"
        const val COLUMN_GAME_ID = "game_id"    // Foreign key to games.id
        const val COLUMN_SKAT_PARTICIPANTS_ID = "skat_participant_id" // Foreign key to skat_participants.id nullable
        const val COLUMN_ROUND = "round"

        const val COLUMN_SCORE_TYPE = "score_type"

        const val COLUMN_WON_OR_LOST = "won_or_lost"

        const val COLUMN_OVERBID_SUIT = "overbid_suit"
        const val COLUMN_OVERBID_BID_VALUE = "overbid_bid_value"

        const val COLUMN_NULL_OPTIONS = "null_options"

        const val COLUMN_GRAND_OR_SUIT_SUIT = "grand_or_suit_suit"
        const val COLUMN_GRAND_OR_SUIT_SPITZEN_VALUE = "grand_or_suit_spitzen_value"
        const val COLUMN_GRAND_OR_SUIT_OPTIONS = "grand_or_suit_options"

        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    object DoppelkopfGamesTable
    {
        const val TABLE_NAME: String = "doppelkopf_games"
        const val COLUMN_ID: String = "id"
        const val COLUMN_TITLE: String = "title"

        const val COLUMN_ROUND_COUNT: String = "round_count"
        const val COLUMN_SCORING_MODE: String = "scoring_mode"

        const val COLUMN_PLAYED_AT = "played_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    object DoppelkopfParticipantsTable
    {
        const val TABLE_NAME: String = "doppelkopf_participants"
        const val COLUMN_ID: String = "id"
        const val COLUMN_GAME_ID: String = "game_id" // Foreign key to doppelkopf_games.id
        const val COLUMN_PLAYER_ID: String = "player_id" // Foreign key to players.id
        const val COLUMN_SEAT: String = "seat"
        const val COLUMN_NAME: String = "name"
    }

    object DoppelkopfScoresTable
    {
        const val TABLE_NAME = "doppelkopf_scores"
        const val COLUMN_ID = "id"
        const val COLUMN_GAME_ID = "game_id" // Foreign key to doppelkopf_games.id
        const val COLUMN_ROUND = "round"

        const val COLUMN_SCORE_TYPE = "score_type"
        const val COLUMN_WINNER = "winner"

        const val COLUMN_RE_PARTICIPANT_1 = "re_participant_1"
        const val COLUMN_RE_PARTICIPANT_2 = "re_participant_2"

        const val COLUMN_SOLOIST_ID = "soloist_id"
        const val COLUMN_SOLO_KIND = "solo_kind"

        const val COLUMN_WIN_LEVEL = "win_level"
        const val COLUMN_ABSAGE = "absage"
        const val COLUMN_RE_ANNOUNCED = "re_announced"
        const val COLUMN_KONTRA_ANNOUNCED = "kontra_announced"
        const val COLUMN_EXTRA_POINTS_RE = "extra_points_re"
        const val COLUMN_EXTRA_POINTS_KONTRA = "extra_points_kontra"

        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }
}