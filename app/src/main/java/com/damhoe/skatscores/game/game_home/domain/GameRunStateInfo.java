package com.damhoe.skatscores.game.game_home.domain;

public class GameRunStateInfo {
   private int roundsCount;
   private int currentRound;
   private boolean isFinished;

   public GameRunStateInfo(int roundsCount, int currentRound, boolean isFinished) {
      this.roundsCount = roundsCount;
      this.currentRound = currentRound;
      this.isFinished = isFinished;
   }

   public int getRoundsCount() {
      return roundsCount;
   }

   public void setRoundsCount(int roundsCount) {
      this.roundsCount = roundsCount;
   }

   public int getCurrentRound() {
      return currentRound;
   }

   public void setCurrentRound(int currentRound) {
      this.currentRound = currentRound;
   }

   public boolean isFinished() {
      return isFinished;
   }

   public void setFinished(boolean finished) {
      isFinished = finished;
   }
}
