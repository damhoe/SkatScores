package com.damhoe.skatscores.game.game_home.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class SkatSettings extends GameSettings {
   private boolean isTournamentScoring;
   private int numberOfRounds;

   public SkatSettings(boolean isTournamentScoring, int numberOfRounds) {
      this.numberOfRounds = numberOfRounds;
      this.isTournamentScoring = isTournamentScoring;
   }

   public boolean isTournamentScoring() {
      return isTournamentScoring;
   }

   public void setTournamentScoring(boolean tournamentScoring) {
      isTournamentScoring = tournamentScoring;
   }

   public int getNumberOfRounds() {
      return numberOfRounds;
   }

   public void setNumberOfRounds(int numberOfRounds) {
      this.numberOfRounds = numberOfRounds;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(@NonNull Parcel out, int flag) {
      out.writeInt(isTournamentScoring ? 1 : 0);
      out.writeInt(numberOfRounds);
   }

   public static final Parcelable.Creator<SkatSettings> CREATOR =
           new Creator<SkatSettings>() {
      @Override
      public SkatSettings createFromParcel(Parcel in) {
         return new SkatSettings(in);
      }

      @Override
      public SkatSettings[] newArray(int size) {
         return new SkatSettings[size];
      }
   };

   private SkatSettings(Parcel in) {
      isTournamentScoring = in.readInt() == 1;
      numberOfRounds = in.readInt();
   }
}
