package com.damhoe.scoresheetskat.player.adapter.out;

import com.damhoe.scoresheetskat.player.domain.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

class PlayerMapper {

   @Inject
   PlayerMapper() {
      // Empty
   }

   protected Player mapToPlayer(PlayerDTO playerDTO, int gameCount) {
      Player player = new Player("");
      player.setID(playerDTO.getId());
      player.setName(playerDTO.getName());
      player.setGameCount(gameCount);
      try {
         player.setCreatedAt(createSimpleDateFormat().parse(playerDTO.getCreatedAt()));
         player.setUpdatedAt(createSimpleDateFormat().parse(playerDTO.getUpdatedAt()));
      } catch (Exception e) {
         e.printStackTrace();
      }
      return player;
   }

   protected PlayerDTO mapPlayerToPlayerDTO(Player player) {
      PlayerDTO playerDTO = new PlayerDTO();
      playerDTO.setId(player.getID());
      playerDTO.setName(player.getName());
      playerDTO.setCreatedAt(getCurrentTimeStamp());
      playerDTO.setUpdatedAt(getCurrentTimeStamp());
      return playerDTO;
   }

   private static SimpleDateFormat createSimpleDateFormat() {
      return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
   }

   private String getCurrentTimeStamp() {
      return createSimpleDateFormat().format(Calendar.getInstance().getTime());
   }

}
