package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.persistence.DatabaseChanges
import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerPersistenceAdapter,
    private val databaseChanges: DatabaseChanges,
) : PlayersRepository
{
    private val _allPlayersState = MutableStateFlow<List<Player>>(emptyList())
    override val allPlayers = _allPlayersState.asStateFlow()

    /**
     * Lives as long as the process, like this repository: the first read has to outlive
     * whatever asked for it, since it belongs to nobody's screen.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * The first read used to be a runBlocking here, which put a database open - and on a
     * first launch every create script with it - on whatever thread Hilt built this on, which
     * is the main one. It is loaded in the background now, so [allPlayers] is briefly empty
     * at startup and everything reading it has to be able to wait: the screens collect it, and
     * the one place that needed a player before the list arrived asks the database directly.
     */
    init
    {
        scope.launch { refreshAll() }
    }

    /*
     * Every suspending call here hops to IO. The adapter below it is blocking, and these are
     * called from viewModelScope, whose dispatcher is the main thread - so without this a
     * profile saved or deleted was a database write on the thread drawing the frame.
     */

    override suspend fun insert(player: Player): Result<Unit> = withContext(Dispatchers.IO) {
        val dto = PlayerDto.mapFrom(player)
        playerDao.insert(dto)
            .onSuccess {
                refreshAll()
                databaseChanges.notifyChanged()
            }
    }

    override suspend fun delete(id: UUID): Result<Player?> = withContext(Dispatchers.IO) {
        playerDao.deletePlayer(id)
            .map { it?.toPlayer() }
            .onSuccess {
                refreshAll()
                databaseChanges.notifyChanged()
            }
    }

    /** Blocking, and stays that way: the one caller wraps it. */
    override fun get(id: UUID): Result<Player?>
    {
        return playerDao.get(id)
            .map { it?.toPlayer() }
    }

    override suspend fun refreshAll() = withContext(Dispatchers.IO) {
        playerDao.getAll()
            .onSuccess { dtoList ->
                val fetchedPlayers = dtoList.map { it.toPlayer() }
                _allPlayersState.value = fetchedPlayers
            }
        Unit
    }

    override suspend fun update(player: Player): Result<Unit> = withContext(Dispatchers.IO) {
        val dto = PlayerDto.mapFrom(player)
        playerDao.updatePlayer(dto)
            .onSuccess {
                refreshAll()
                databaseChanges.notifyChanged()
            }
    }
}
