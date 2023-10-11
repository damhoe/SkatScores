package com.damhoe.scoresheetskat.score.domain;

import com.damhoe.scoresheetskat.score.Constant;

public final class SkatScoreToPointsConverter {

   public static int convertSuitScore(SkatScore score) {
      int spielwert = calculateSpielwert(score);
      int points = 0;
      switch (score.getSuit()) {
         case CLUBS:
            points = spielwert * Constant.POINTS_CLUBS;
            break;
         case HEARTS:
            points = spielwert * Constant.POINTS_HEARTS;
            break;
         case DIAMONDS:
            points = spielwert * Constant.POINTS_DIAMONDS;
            break;
         case SPADES:
            points = spielwert * Constant.POINTS_SPADES;
            break;
         default:
            throw new IllegalArgumentException("Invalid suit: " + score.getSuit());
      }
      return points;
   }

   private static int calculateSpielwert(SkatScore score) {
      return new SpielwertBuilder(score)
              .addHand()
              .addOuvert()
              .addSchneider()
              .addSchwarz()
              .build();
   }

   public static int convertGrandScore(SkatScore score) {
      int spielwert = calculateSpielwert(score);
      return spielwert * Constant.POINTS_GRAND;
   }

   public static int convertNullScore(SkatScore score) {
      if (score.isHand()) {
         if (score.isOuvert()) {
            return Constant.POINTS_NULL_OUVERT_HAND;
         }
         return Constant.POINTS_NULL_HAND;
      }
      if (score.isOuvert()) {
         return Constant.POINTS_NULL_OUVERT;
      }
      return Constant.POINTS_NULL;
   }

   static class SpielwertBuilder {
      private int winLevels;
      private final SkatScore score;

      SpielwertBuilder(SkatScore score) {
         winLevels = 0;
         this.score = score;
      }

      SpielwertBuilder addHand() {
         if (score.isHand())
            winLevels += 1;
         return this;
      }

      SpielwertBuilder addOuvert() {
         if (score.isOuvert()) {
            winLevels += 1;
         }
         return this;
      }

      SpielwertBuilder addSchneider() {
         if (score.isSchneider()) {
            winLevels += 1;
            if (score.isSchneiderAnnounced())
               winLevels += 1;
         }
         return this;
      }

      SpielwertBuilder addSchwarz() {
         if (score.isSchwarz()) {
            winLevels += 1;
            if (score.isSchwarzAnnounced())
               winLevels += 1;
         }
         return this;
      }

      int build() {
         int lossFactor = score.isWon() ? 1 : -2;
         int value = Math.abs(score.getSpitzen()) + 1 + winLevels;
         return value * lossFactor;
      }
   }
}
