package com.damhoe.skatscores.game.game_home.adapter.out.models;

import java.util.UUID;

public class PlayerMatchDTO {
   private long mId;
   private long mGameId;
   private long mPlayerId;
   private int mPosition;

   public PlayerMatchDTO(long id, long gameId, long playerId, int position) {
      mId = id;
      mGameId = gameId;
      mPlayerId = playerId;
      mPosition = position;
   }

   public PlayerMatchDTO(long gameId, long playerId, int position) {
      mId = UUID.randomUUID().getMostSignificantBits();
      mGameId = gameId;
      mPlayerId = playerId;
      mPosition = position;
   }

   public long getGameId() {
      return mGameId;
   }

   public void setGameId(long gameId) {
      this.mGameId = gameId;
   }

   public int getPosition() {
      return mPosition;
   }

   public void setPosition(int position) {
      this.mPosition = position;
   }

   public long getId() {
      return mId;
   }

   public void setId(long id) {
      this.mId = id;
   }

   public long getPlayerId() {
      return mPlayerId;
   }

   public void setPlayerId(long playerId) {
      this.mPlayerId = playerId;
   }
}
