package com.damhoe.scoresheetskat.game.domain;

import java.util.UUID;

public class PlayerMatch {
   private long id;
   private long gameId;
   private int position;

   public PlayerMatch(long gameId, int position) {
      id = UUID.randomUUID().getMostSignificantBits();
      this.gameId = gameId;
      this.position = position;
   }

   public long getGameId() {
      return gameId;
   }

   public void setGameId(long gameId) {
      this.gameId = gameId;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }
}
