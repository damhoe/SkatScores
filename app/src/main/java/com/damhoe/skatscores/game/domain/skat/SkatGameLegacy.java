package com.damhoe.skatscores.game.domain.skat;

import com.damhoe.skatscores.game.domain.Game;
import com.damhoe.skatscores.game.application.TotalPointsCalculator;
import com.damhoe.skatscores.game.domain.score.SkatScore;

public class SkatGameLegacy extends Game<SkatSettings, SkatScore> {
   private int currentRound;
   private int startDealerPosition;

   public SkatGameLegacy(
           SkatGameBuilder builder)
   {
      super(
              builder.mTitle,
              builder.mPlayers,
              builder.mSettings);
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
           Builder<SkatGameLegacy, SkatSettings, SkatScore> {
      protected int mStartDealerPosition = 0;

      @Override
      public SkatGameLegacy build() {
         return new SkatGameLegacy(this);
      }

      public SkatGameBuilder setStartDealerPosition(int position) {
         mStartDealerPosition = position;
         return this;
      }
   }

   @Override
   public void start()
   {
      super.start();
      currentRound = 1;
   }

   public int[] calculateTotalPoints() {
      return TotalPointsCalculator.INSTANCE.calculateTotalPoints(
              getPlayers().size(), getScores(), settings.isTournamentScoring);
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
      if (currentRound > settings.roundCount) {
         finish();
      }
   }

   @Override
   public void finish() {
      super.finish();
   }

   @Override
   public void setSettings(SkatSettings settings) {
      super.settings = settings;
      if (currentRound > settings.roundCount) {
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

      if (currentRound <= settings.roundCount) {
         resume();
      }

      return super.removeLastScore();
   }
}
