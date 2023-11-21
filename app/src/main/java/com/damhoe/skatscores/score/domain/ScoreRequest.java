package com.damhoe.skatscores.score.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ScoreRequest implements Parcelable {
   private final long scoreId;
   private final long gameId;
   private final String[] playerNames;
   private final int[] playerPositions;

   public ScoreRequest(Builder builder) {
      scoreId = builder.scoreId;
      gameId = builder.gameId;
      playerNames = builder.playerNames;
      playerPositions = builder.playerPositions;
   }

   public long getGameId() {
      return gameId;
   }

   public int[] getPlayerPositions() {
      return playerPositions;
   }

   public long getScoreId() {
      return scoreId;
   }

   public String[] getPlayerNames() {
      return playerNames;
   }

   public static class Builder {
      private long scoreId = -1L;
      private long gameId = -1L;
      private final String[] playerNames = new String[3];
      private final int[] playerPositions = new int[] { 0, 1, 2 };

      public Builder setScoreId(long scoreId) {
         this.scoreId = scoreId;
         return this;
      }

      public Builder setGameId(long gameId) {
         this.gameId = gameId;
         return this;
      }

      public Builder setPlayerNames(String player1, String player2, String player3) {
         playerNames[0] = player1;
         playerNames[1] = player2;
         playerNames[2] = player3;
         return this;
      }

      public Builder setPlayerPositions(int positionPlayer1, int positionPlayer2, int positionPlayer3) {
         playerPositions[0] = positionPlayer1;
         playerPositions[1] = positionPlayer2;
         playerPositions[2] = positionPlayer3;
         return this;
      }

      public ScoreRequest build() {
         return new ScoreRequest(this);
      }
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(@NonNull Parcel parcel, int flag) {
      parcel.writeLong(scoreId);
      parcel.writeLong(gameId);
      parcel.writeString(playerNames[0]);
      parcel.writeString(playerNames[1]);
      parcel.writeString(playerNames[2]);
      parcel.writeInt(playerPositions[0]);
      parcel.writeInt(playerPositions[1]);
      parcel.writeInt(playerPositions[2]);
   }

   public static final Parcelable.Creator<ScoreRequest> CREATOR =
           new Parcelable.Creator<ScoreRequest>() {

      @Override
      public ScoreRequest createFromParcel(Parcel in) {
         return new ScoreRequest(in);
      }

      @Override
      public ScoreRequest[] newArray(int size) {
         return new ScoreRequest[size];
      }
   };

   private ScoreRequest(Parcel in) {
      scoreId = in.readLong();
      gameId = in.readLong();
      playerNames = new String[3];
      playerPositions = new int[3];
      playerNames[0] = in.readString();
      playerNames[1] = in.readString();
      playerNames[2] = in.readString();
      playerPositions[0] = in.readInt();
      playerPositions[1] = in.readInt();
      playerPositions[2] = in.readInt();
   }
}
