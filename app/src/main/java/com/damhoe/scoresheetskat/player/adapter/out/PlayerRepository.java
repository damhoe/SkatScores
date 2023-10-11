package com.damhoe.scoresheetskat.player.adapter.out;

import android.content.res.Resources;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.base.SearchResult;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.LoadPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PlayerRepository implements CreatePlayerPort, LoadPlayerPort, UpdatePlayerPort {

   private final PlayerPersistenceAdapter adapter;
   private final PlayerMapper playerMapper;

   @Inject
   PlayerRepository(PlayerPersistenceAdapter adapter, PlayerMapper mapper) {
      this.adapter = adapter;
      this.playerMapper = mapper;
   }

   @Override
   public boolean isPlayerNameExistent(String name) {
      return adapter.isPlayerNameExistent(name);
   }

   @Override
   public Player createPlayer(String name) {
      Player player = new Player(name);
      PlayerDTO playerDTO = playerMapper.mapPlayerToPlayerDTO(player);
      long id = adapter.insertPlayer(playerDTO);
      player.setID(id);
      return player;
   }

   @Override
   public Player deletePlayer(long id) {
      Result<PlayerDTO> result = adapter.deletePlayer(id);
      if (result.isFailure()) {
         throw new Resources.NotFoundException(result.getMessage());
      }
      return playerMapper.mapPlayerDTOtoPlayer(result.getValue());
   }

   @Override
   public Player loadPlayer(long id) {
      Result<PlayerDTO> result = adapter.getPlayer(id);
      if (result.isFailure()) {
         throw new Resources.NotFoundException(result.getMessage());
      }
      return playerMapper.mapPlayerDTOtoPlayer(result.getValue());
   }

   @Override
   public List<Player> loadAllPlayers() {
      List<PlayerDTO> playerDTOS = adapter.loadAllPlayers();
      List<Player> players = new ArrayList<>();
      for (int k = 0; k < playerDTOS.size(); k++) {
         players.add(playerMapper.mapPlayerDTOtoPlayer(playerDTOS.get(k)));
      }
      return players;
   }

   @Override
   public SearchResult<Player> searchByName(String name) {
      return null;
   }

   @Override
   public boolean updateName(long id, String newName) {
      Player player = new Player(newName);
      player.setID(id);
      int updateResult = adapter.updatePlayer(playerMapper.mapPlayerToPlayerDTO(player));
      return updateResult == 1;
   }
}
