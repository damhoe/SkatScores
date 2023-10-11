package com.damhoe.scoresheetskat.player.adapter.out;

import android.text.style.TtsSpan;

import com.damhoe.scoresheetskat.player.domain.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

class PlayerMapper {

   @Inject
   PlayerMapper() {
      // Empty
   }

   protected Player mapPlayerDTOtoPlayer(PlayerDTO playerDTO) {
      Player player = new Player("");
      player.setID(playerDTO.getId());
      player.setName(playerDTO.getName());
      try {
         player.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(playerDTO.getCreatedAt()));
         player.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(playerDTO.getUpdatedAt()));
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

   private String getCurrentTimeStamp() {
      return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
   }

}
