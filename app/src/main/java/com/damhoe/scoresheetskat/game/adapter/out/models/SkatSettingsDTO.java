package com.damhoe.scoresheetskat.game.adapter.out.models;

public class SkatSettingsDTO {
   private long id;
   private int numberOfRounds;
   private int scoringType;

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public int getNumberOfRounds() {
      return numberOfRounds;
   }

   public void setNumberOfRounds(int numberOfRounds) {
      this.numberOfRounds = numberOfRounds;
   }

   public int getScoringType() {
      return scoringType;
   }

   public void setScoringType(int scoringType) {
      this.scoringType = scoringType;
   }
}
