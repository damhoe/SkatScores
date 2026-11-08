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
    private val processScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Empty until the first read completes; use [get] when a player is needed right away. */
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
            .stateIn(processScope, SharingStarted.Eagerly, emptyList())

    override suspend fun insert(player: Player): Result<Unit> = withContext(Dispatchers.IO) {
        playerDao.insert(PlayerDto.mapFrom(player))
            .onSuccess { databaseChanges.notifyChanged(DataTopic.PLAYERS) }
    }

    override suspend fun delete(id: UUID): Result<Player?> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction { playerDao.delete(id).getOrThrow() }
            .mapCatching { it?.toPlayer() }
            .onSuccess { notifyEverything() }
    }

    override suspend fun get(id: UUID): Result<Player?> = withContext(Dispatchers.IO) {
        playerDao.get(id).mapCatching { it?.toPlayer() }
    }

    override suspend fun update(player: Player): Result<Unit> = withContext(Dispatchers.IO) {
        dbHelper
            .transaction { playerDao.rename(PlayerDto.mapFrom(player)).getOrThrow() }
            .onSuccess { notifyEverything() }
    }

    /** Player writes also change seats in the lists. */
    private fun notifyEverything() = databaseChanges.notifyChanged(
        DataTopic.PLAYERS,
        DataTopic.SKAT,
        DataTopic.DOPPELKOPF,
    )
}
