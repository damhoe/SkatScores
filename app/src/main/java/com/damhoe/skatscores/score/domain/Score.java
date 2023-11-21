package com.damhoe.skatscores.score.domain;

public abstract class Score {
   protected long id;
   protected int playerPosition;
   protected long gameId;
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

   public void setPlayerPosition(int playerPosition) {
      this.playerPosition = playerPosition;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

}
