package com.damhoe.scoresheetskat.game.adapter.out;

import com.damhoe.scoresheetskat.game.adapter.out.models.SkatGameDTO;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public final class GameMapper {

   public static SkatGame mapSkatGameDTOToSkatGame(
           SkatGameDTO skatGameDTO,
           SkatSettings skatSettings,
           List<Player> players
   ) {
      // Create game builder
      SkatGame game = new SkatGame.SkatGameBuilder()
              .setStartDealerPosition(skatGameDTO.getStartDealerPosition())
              .setTitle(skatGameDTO.getTitle())
              .setSettings(skatSettings)
              .setPlayers(players)
              .build();

      game.setId(skatGameDTO.getId());

      try {
         game.setCreatedAt(createSimpleDateFormat().parse(skatGameDTO.getCreatedAt()));
      } catch (Exception e) {
         e.printStackTrace();
      }
      return game;
   }

   public static SkatGamePreview mapToPreview(
           SkatGameDTO skatGameDTO,
           List<Player> players
   ) {
      SkatGamePreview skatGamePreview = new SkatGamePreview();
      skatGamePreview.setGameId(skatGameDTO.getId());
      skatGamePreview.setTitle(skatGameDTO.getTitle());
      skatGamePreview.setPlayerNames(getPlayerNames(players));
      skatGamePreview.setFinished(false);

      try {
         skatGamePreview.setDate(createSimpleDateFormat().parse(skatGameDTO.getCreatedAt()));
      } catch (Exception e) {
         e.printStackTrace();
      }

      return skatGamePreview;
   }

   public static SkatGameDTO mapSkatGameToSkatGameDTO(SkatGame skatGame, long settingsId) {
      SkatGameDTO skatGameDTO = new SkatGameDTO();
      skatGameDTO.setTitle(skatGame.getTitle());
      skatGameDTO.setStartDealerPosition(skatGame.getStartDealerPosition());
      skatGameDTO.setSettingsId(settingsId);
      skatGameDTO.setCreatedAt(createSimpleDateFormat().format(skatGame.getCreatedAt()));
      skatGameDTO.setUpdatedAt(getCurrentTimeStamp());
      return skatGameDTO;
   }

   private static SimpleDateFormat createSimpleDateFormat() {
      return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
   }

   private static String getCurrentTimeStamp() {
      return createSimpleDateFormat().format(Calendar.getInstance().getTime());
   }

   private static List<String> getPlayerNames(List<Player> players) {
      List<java.lang.String> names = new ArrayList<>();
      for (Player player: players) {
         names.add(player.getName());
      }
      return names;
   }

}
