package com.damhoe.scoresheetskat.game.domain;

import com.damhoe.scoresheetskat.game.application.TotalPointsCalculator;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

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
      currentRound = 1;
      super.start();
   }

   public int[] calculateTotalPoints() {
      return TotalPointsCalculator.calculateTotalPoints(
              getPlayers().size(), getScores(), getSettings().isTournamentScoring());
   }

   public int[] calculateWinBonus() {
      return TotalPointsCalculator.calculateWinBonus(getPlayers().size(), getScores());
   }

   public int[] calculateLossOfOthersBonus() {
      return TotalPointsCalculator.calculateLossOfOthersBonus(getPlayers().size(), getScores());
   }

   @Override
   public void addScore(SkatScore score) {
      super.addScore(score);
      currentRound += 1;
   }

   @Override
   public SkatScore removeLastScore() {
      if (currentRound > 1)
         currentRound -= 1;
      return super.removeLastScore();
   }
}
