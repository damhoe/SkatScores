package com.damhoe.skatscores.game.game_home.domain;

import com.damhoe.skatscores.game.game_home.application.TotalPointsCalculator;
import com.damhoe.skatscores.game.score.domain.SkatScore;

public class SkatGame extends Game<SkatSettings, SkatScore> {
   private int currentRound;
   private int startDealerPosition;

   public SkatGame(SkatGameBuilder builder) {
      super(builder.mTitle, builder.mPlayers, builder.mSettings);
      startDealerPosition = builder.mStartDealerPosition;
   }

   public int getCurrentRound() {
      return currentRound;
   }

   public int getStartDealerPosition() {
      return startDealerPosition;
   }

   public void setStartDealerPosition(int startDealerPosition) {
      this.startDealerPosition = startDealerPosition;
   }

   public static class SkatGameBuilder extends
           Game.Builder<SkatGame, SkatSettings, SkatScore> {
      protected int mStartDealerPosition = 0;

      @Override
      public SkatGame build() {
         return new SkatGame(this);
      }

      public SkatGameBuilder setStartDealerPosition(int position) {
         mStartDealerPosition = position;
         return this;
      }
   }

   @Override
   public void start() {
      super.start();
      currentRound = 1;
   }

   public int[] calculateTotalPoints() {
      return TotalPointsCalculator.INSTANCE.calculateTotalPoints(
              getPlayers().size(), getScores(), getSettings().isTournamentScoring());
   }

   public int[] calculateWinBonus() {
      return TotalPointsCalculator.INSTANCE.calculateWinBonus(getPlayers().size(), getScores());
   }

   public int[] calculateLossOfOthersBonus() {
      return TotalPointsCalculator.INSTANCE.calculateLossOfOthersBonus(getPlayers().size(), getScores());
   }

   @Override
   public void addScore(SkatScore score) {
      super.addScore(score);
      currentRound += 1;
      if (currentRound > settings.getNumberOfRounds()) {
         finish();
      }
   }

   @Override
   public void finish() {
      super.finish();
   }

   @Override
   public void setSettings(SkatSettings settings) {
      super.setSettings(settings);
      if (currentRound > settings.getNumberOfRounds()) {
         finish();
      } else {
         resume();
      }
   }

   @Override
   public SkatScore removeLastScore() {
      if (currentRound > 1) {
         currentRound -= 1;
      }

      if (currentRound <= settings.getNumberOfRounds()) {
         resume();
      }

      return super.removeLastScore();
   }
}
