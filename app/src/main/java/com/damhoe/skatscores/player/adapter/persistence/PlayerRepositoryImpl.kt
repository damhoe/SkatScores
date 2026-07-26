package com.damhoe.skatscores.player.adapter.persistence

import com.damhoe.skatscores.player.application.repositories.PlayersRepository
import com.damhoe.skatscores.player.domain.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerPersistenceAdapter
) : PlayersRepository
{
    private val _allPlayersState = MutableStateFlow<List<Player>>(emptyList())
    override val allPlayers = _allPlayersState.asStateFlow()

    init
    {
        runBlocking { refreshAll() }
    }

    override suspend fun insert(player: Player): Result<Unit>
    {
        val dto = PlayerDto.mapFrom(player)
        return playerDao.insert(dto)
            .onSuccess {
                refreshAll()
            }
    }

    override suspend fun delete(id: UUID): Result<Player?>
    {
        return playerDao.deletePlayer(id)
            .map { it?.toPlayer() }
            .onSuccess { refreshAll() }
    }

    override fun get(id: UUID): Result<Player?>
    {
        return playerDao.get(id)
            .map { it?.toPlayer() }
    }

    override suspend fun refreshAll()
    {
        playerDao.getAll()
            .onSuccess { dtoList ->
                val fetchedPlayers = dtoList.map { it.toPlayer() }
                _allPlayersState.value = fetchedPlayers
            }
    }

    override suspend fun update(player: Player): Result<Unit>
    {
        val dto = PlayerDto.mapFrom(player)
        return playerDao.updatePlayer(dto)
            .onSuccess { refreshAll() }
    }
}
