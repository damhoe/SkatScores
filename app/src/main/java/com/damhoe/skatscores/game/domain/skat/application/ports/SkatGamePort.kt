package com.damhoe.skatscores.game.domain.skat.application.ports

import androidx.lifecycle.LiveData
import com.damhoe.skatscores.game.domain.skat.SkatGame
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview
import java.util.Date
import java.util.UUID

interface SkatGamePort
{
    fun add(game: SkatGame): SkatGame

    fun get(id: UUID): SkatGame

    fun delete(id: UUID): SkatGame

    fun update(game: SkatGame): SkatGame

    val unfinishedGames: LiveData<List<SkatGamePreview>>

    fun getGamesSince(since: Date): LiveData<List<SkatGamePreview>>

    fun refreshGamePreviews()
}
