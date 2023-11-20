package com.damhoe.scoresheetskat.game.adapter.out;

import com.damhoe.scoresheetskat.KotlinResultWrapper;
import com.damhoe.scoresheetskat.game.adapter.out.models.SkatGameDTO;
import com.damhoe.scoresheetskat.game.domain.GameRunStateInfo;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.score.application.ports.in.GetScoreUseCase;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

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

   public SkatGame mapSkatGameDTOToSkatGame(
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

      // Add Scores
      List<SkatScore> scores = KotlinResultWrapper.Companion.getScores(mGetScoreUseCase, skatGameDTO.getId());
      for (int i = 0; i < scores.size(); i++) {
         game.addScore(scores.get(i));
      }

      try {
         game.setCreatedAt(createSimpleDateFormat().parse(skatGameDTO.getCreatedAt()));
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
      int roundsCount = settings.getNumberOfRounds();
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
