package com.damhoe.scoresheetskat.game.adapter.out;

import com.damhoe.scoresheetskat.game.application.ports.out.GamePort;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;
import com.damhoe.scoresheetskat.game_setup.domain.SkatGameCommand;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameRepository implements GamePort {
   private final GamePersistenceAdapter adapter;
   private SkatGame skatGame;

   @Inject
   public GameRepository(GamePersistenceAdapter adapter) {
      this.adapter = adapter;
   }

   @Override
   public SkatGame saveSkatGame(SkatGame game) {
      this.skatGame = game;
      return this.skatGame;
   }

   @Override
   public boolean addScore(long gameId, SkatScore score) {
      if (gameId != skatGame.getId()) {
         return false;
      }
      //skatGame.addScore(score);
      return true;
   }

   @Override
   public SkatGame deleteSkatGame(long id) {
      return this.skatGame;
   }

   @Override
   public SkatGame loadSkatGame(long id) {
      SkatGameCommand cmd = new SkatGameCommand(
              "A fun skat evening.",
              3,
              new SkatSettings(true, 18));
      skatGame = new SkatGame.SkatGameBuilder()
              .fromCommand(cmd)
              .build();
      skatGame.setId(id);
      return skatGame;
   }
}
