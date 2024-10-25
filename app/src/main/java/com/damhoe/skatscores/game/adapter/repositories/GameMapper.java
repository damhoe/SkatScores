package com.damhoe.skatscores.game.adapter.repositories;

import com.damhoe.skatscores.KotlinResultWrapper;
import com.damhoe.skatscores.game.domain.skat.SkatGameLegacy;
import com.damhoe.skatscores.game.domain.GameRunStateInfo;
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;
import com.damhoe.skatscores.game.domain.skat.SkatSettings;
import com.damhoe.skatscores.game.domain.skat.application.ports.GetScoreUseCase;
import com.damhoe.skatscores.game.domain.score.SkatScore;
import com.damhoe.skatscores.player.domain.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class GameMapper {

   private final GetScoreUseCase mGetScoreUseCase;

   @Inject
   public GameMapper(GetScoreUseCase getScoreUseCase) {
      mGetScoreUseCase = getScoreUseCase;
   }

   public SkatGameLegacy mapSkatGameDTOToSkatGame(
           SkatGameDTO skatGameDTO,
           SkatSettings skatSettings,
           List<Player> players
   ) {
      // Create game builder
      SkatGameLegacy game = new SkatGameLegacy.SkatGameBuilder()
              .setStartDealerPosition(skatGameDTO.getStartDealerPosition())
              .setTitle(skatGameDTO.getTitle())
              .setSettings(skatSettings)
              .setPlayers(players)
              .build();

      game.id = skatGameDTO.getId();

      // Add Scores
      List<SkatScore> scores = KotlinResultWrapper.Companion.getScores(mGetScoreUseCase, skatGameDTO.getId());
      for (int i = 0; i < scores.size(); i++) {
         game.addScore(scores.get(i));
      }

      try {
         game.createdAt = createSimpleDateFormat().parse(skatGameDTO.getCreatedAt());
      } catch (Exception e) {
         e.printStackTrace();
      }
      return game;
   }

   public SkatGamePreview mapToPreview(
           SkatGameDTO skatGameDTO,
           SkatSettings settings,
           List<Player> players
   ) {
      SkatGamePreview skatGamePreview = new SkatGamePreview();
      skatGamePreview.setGameId(skatGameDTO.getId());
      skatGamePreview.setTitle(skatGameDTO.getTitle());
      skatGamePreview.setPlayerNames(getPlayerNames(players));

      // Add Scores
      List<SkatScore> scores = KotlinResultWrapper.Companion.getScores(mGetScoreUseCase, skatGameDTO.getId());
      int scoreCount = scores.size();
      int roundsCount = settings.roundCount;
      skatGamePreview.setGameRunStateInfo(
              new GameRunStateInfo(
                      roundsCount,
                      scoreCount + 1,
                      roundsCount == scoreCount
              )
      );

      try {
         skatGamePreview.setDate(createSimpleDateFormat().parse(skatGameDTO.getCreatedAt()));
      } catch (Exception e) {
         e.printStackTrace();
      }

      return skatGamePreview;
   }

   public static SkatGameDTO mapSkatGameToSkatGameDTO(SkatGameLegacy skatGame, long settingsId) {
      SkatGameDTO skatGameDTO = new SkatGameDTO();
      skatGameDTO.setTitle(skatGame.title);
      skatGameDTO.setStartDealerPosition(skatGame.getStartDealerPosition());
      skatGameDTO.setSettingsId(settingsId);
      skatGameDTO.setCreatedAt(createSimpleDateFormat().format(skatGame.createdAt));
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
         names.add(player.name);
      }
      return names;
   }
}
