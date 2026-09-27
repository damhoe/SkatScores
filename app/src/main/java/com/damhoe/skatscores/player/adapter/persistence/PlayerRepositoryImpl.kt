package com.damhoe.skatscores.player.adapter.persistence

import android.util.Log
import com.damhoe.skatscores.persistence.DataTopic
import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlayerRepository"

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val dbHelper: DatabaseHelper,
    private val playerDao: PlayerPersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : PlayersRepository
{
    /** Lives as long as the process, like this repository: the list belongs to no screen. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Read again after every write to the players, the newest read winning. This used to be
     * refreshed by hand after each write, from as many coroutines at once, and a slow first
     * read could land after a newer one - a player added at startup then vanished from the
     * list. It is empty for a moment at startup, so everything reading it has to be able to
     * wait: the screens collect it, and the one place that needs a player before it arrives
     * asks [get].
     */
    override val allPlayers: StateFlow<List<Player>> =
        databaseChanges.changes(DataTopic.PLAYERS)
            .mapLatest {
                playerDao.getAll()
                    .mapCatching { dtos -> dtos.map { it.toPlayer() } }
                    .getOrElse { e ->
                        Log.e(TAG, "Could not read players", e)
                        emptyList()
                    }
            }
            .flowOn(Dispatchers.IO)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /*
     * Every suspending call here hops to IO. The adapter below it is blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread.
     */

    override suspend fun insert(player: Player): Result<Unit> = withContext(Dispatchers.IO) {
        playerDao.insert(PlayerDto.mapFrom(player))
            .onSuccess { databaseChanges.notifyChanged(DataTopic.PLAYERS) }
    }

    /** Deleting takes the player off every list, so the lists are told as well. */
    override suspend fun delete(id: UUID): Result<Player?> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction { playerDao.delete(id).getOrThrow() }
            .mapCatching { it?.toPlayer() }
            .onSuccess { notifyEverything() }
    }

    override suspend fun get(id: UUID): Result<Player?> = withContext(Dispatchers.IO) {
        playerDao.get(id).mapCatching { it?.toPlayer() }
    }

    /** A new name reaches the seats linked to the player, so the lists are told as well. */
    override suspend fun update(player: Player): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction { playerDao.rename(PlayerDto.mapFrom(player)).getOrThrow() }
            .onSuccess { notifyEverything() }
    }

    private fun notifyEverything() = databaseChanges.notifyChanged(
        DataTopic.PLAYERS,
        DataTopic.SKAT,
        DataTopic.DOPPELKOPF,
    )
}
