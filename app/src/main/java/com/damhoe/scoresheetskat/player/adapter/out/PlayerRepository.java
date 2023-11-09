package com.damhoe.scoresheetskat.player.adapter.out;

import android.content.res.Resources;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.GetPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class PlayerRepository implements CreatePlayerPort, GetPlayerPort, UpdatePlayerPort {

   private final PlayerPersistenceAdapter mPersistenceAdapter;
   private final PlayerMapper mMapper;

   private final MutableLiveData<List<PlayerDTO>> players = new MutableLiveData<>();

   @Inject
   PlayerRepository(PlayerPersistenceAdapter persistenceAdapter, PlayerMapper mapper) {
      mPersistenceAdapter = persistenceAdapter;
      mMapper = mapper;

      // Load data
      players.setValue(persistenceAdapter.loadAllPlayers());
   }

   @Override
   public Player createPlayer(String name) {
      Player player = new Player(name);
      PlayerDTO playerDTO = mMapper.mapPlayerToPlayerDTO(player);
      long id = mPersistenceAdapter.insertPlayer(playerDTO);
      player.setID(id);

      // Update player list
      List<PlayerDTO> currentPlayers = players.getValue();
      if (currentPlayers != null) {
         currentPlayers.add(playerDTO);
         players.postValue(currentPlayers);
      }

      return player;
   }

   @Override
   public Player deletePlayer(long id) {
      Result<PlayerDTO> result = mPersistenceAdapter.deletePlayer(id);
      if (result.isFailure()) {
         throw new Resources.NotFoundException(result.getMessage());
      }

      // Update player list
      List<PlayerDTO> currentPlayers = players.getValue();
      if (currentPlayers != null) {
         currentPlayers.removeIf(player -> player.getId() == id);
         players.postValue(currentPlayers);
      }

      int gameCount = mPersistenceAdapter.getGameCount(id);
      return mMapper.mapToPlayer(result.getValue(), gameCount);
   }

   @Override
   public Player getPlayer(long id) {
      Result<PlayerDTO> result = mPersistenceAdapter.getPlayer(id);
      if (result.isFailure()) {
         throw new Resources.NotFoundException(result.getMessage());
      }
      int gameCount = mPersistenceAdapter.getGameCount(id);
      return mMapper.mapToPlayer(result.getValue(), gameCount);
   }

   @Override
   public LiveData<List<Player>> getPlayersLiveData() {
      return Transformations.map(players,
              playerDtoList -> playerDtoList.stream()
                      .map(dto -> {
                         int gameCount = mPersistenceAdapter.getGameCount(dto.getId());
                         return mMapper.mapToPlayer(dto, gameCount);
                      })
                      .collect(Collectors.toList()));
   }

   /** @noinspection DataFlowIssue*/
   @Override
   public List<Player> getPlayers() {
      return players.getValue().stream()
              .map(dto -> {
                 int gameCount = mPersistenceAdapter.getGameCount(dto.getId());
                 return mMapper.mapToPlayer(dto, gameCount);
              })
              .collect(Collectors.toList());
   }

   @Override
   public void refreshPlayersFromRepository() {
      players.postValue(mPersistenceAdapter.loadAllPlayers());
   }

   @Override
   public Player updateName(long id, String newName) {
      Player player = getPlayer(id);
      player.setName(newName);
      // Convert to database model
      PlayerDTO dto = mMapper.mapPlayerToPlayerDTO(player);
      // Persist changes
      boolean isSuccess = mPersistenceAdapter.updatePlayer(dto) == 1;

      // Update player list
      List<PlayerDTO> currentPlayers = players.getValue();
      if (isSuccess && currentPlayers != null) {

         // Replace old player with updated version
         for (int i = 0; i < currentPlayers.size(); i++) {
            if (currentPlayers.get(i).getId() == id) {
               currentPlayers.set(i, dto);
               break;
            }
         }

         players.postValue(currentPlayers);
      }

      return player;
   }
}
