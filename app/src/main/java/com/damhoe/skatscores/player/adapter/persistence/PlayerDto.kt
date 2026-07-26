package com.damhoe.skatscores.player.adapter.persistence

import android.database.Cursor
import androidx.core.content.contentValuesOf
import com.damhoe.skatscores.persistence.DatabaseConstants.PlayersTable
import com.damhoe.skatscores.persistence.DatabaseConstants.SkatGamesTable
import com.damhoe.skatscores.persistence.getStringOrNull
import com.damhoe.skatscores.persistence.getUuidOrNull
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import java.time.Instant
import java.util.UUID

class PlayerDto(
    val id: UUID,
    val name: String,
    var createdAt: Instant,
)
{
    fun toPlayer(): Player
    {
        return Player(
            id,
            PlayerName.create(name).getOrThrow(),
            createdAt,
        )
    }

    fun toContentValues() = contentValuesOf(
        PlayersTable.COLUMN_ID to id.toString(),
        PlayersTable.COLUMN_NAME to name,
    )

    companion object
    {
        fun mapFrom(player: Player): PlayerDto
        {
            return PlayerDto(
                player.id,
                player.name.value,
                player.createdAt,
            )
        }

        fun mapFrom(cursor: Cursor): PlayerDto
        {
            val id = cursor.getUuidOrNull(SkatGamesTable.COLUMN_ID)!!
            val name = cursor.getStringOrNull(PlayersTable.COLUMN_NAME)!!

            val createdAtString = cursor.getStringOrNull(PlayersTable.COLUMN_CREATED_AT)
            val createdAt = Instant.parse(createdAtString)

            return PlayerDto(
                id = id,
                name = name,
                createdAt = createdAt,
            )
        }
    }
}
