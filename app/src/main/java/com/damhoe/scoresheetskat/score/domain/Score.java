package com.damhoe.scoresheetskat.score.domain;

public abstract class Score {
   protected int playerPosition;
   protected long gameId;
   protected int index;
   public abstract int toPoints();

   public long getGameId() {
      return gameId;
   }

   public void setGameId(long gameId) {
      this.gameId = gameId;
   }

   public int getPlayerPosition() {
      return playerPosition;
   }

   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
   }
}
