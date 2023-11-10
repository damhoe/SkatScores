package com.damhoe.scoresheetskat.statistics.domain;

public class ProgressInfo {
   private int gamesCount;
   private int openGamesCount;

   public ProgressInfo(int gamesCount, int openGamesCount) {
      this.gamesCount = gamesCount;
      this.openGamesCount = openGamesCount;
   }

   public double toPercent() {
      return 100. * (1. - (double) openGamesCount / gamesCount);
   }

   public int getGamesCount() {
      return gamesCount;
   }

   public void setGamesCount(int gamesCount) {
      this.gamesCount = gamesCount;
   }

   public int getOpenGamesCount() {
      return openGamesCount;
   }

   public void setOpenGamesCount(int openGamesCount) {
      this.openGamesCount = openGamesCount;
   }
}
