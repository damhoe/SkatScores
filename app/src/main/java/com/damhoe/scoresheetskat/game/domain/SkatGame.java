package com.damhoe.scoresheetskat.game.domain;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

public class SkatGame extends Game<SkatSettings, SkatScore> {
   private int currentRound;
   private int firstDealerPosition;

   public SkatGame(SkatGameBuilder builder) {
      super(builder.title, builder.players, builder.settings);
      firstDealerPosition = 0;
   }

   public int getCurrentRound() {
      return currentRound;
   }

   public int getFirstDealerPosition() {
      return firstDealerPosition;
   }

   public void setFirstDealerPosition(int firstDealerPosition) {
      this.firstDealerPosition = firstDealerPosition;
   }

   public static class SkatGameBuilder extends
           Game.Builder<SkatGame, SkatSettings, SkatScore> {
      @Override
      public SkatGame build() {
         return new SkatGame(this);
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
