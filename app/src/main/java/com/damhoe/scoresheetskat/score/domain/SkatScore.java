package com.damhoe.scoresheetskat.score.domain;

import java.util.UUID;

public class SkatScore extends Score {
   private long id;
   private final int spitzen; // negative values if missing
   private final SkatSuit suit; // Clubs, Diamonds, Hears, Spades, Null, Grand
   private final boolean hand;
   private final boolean schneider;
   private final boolean schneiderAnnounced;
   private final boolean schwarz;
   private final boolean schwarzAnnounced;
   private final boolean ouvert;
   private final boolean isWon;
   private final boolean isPasse;

   public SkatScore(SkatScoreCommand command) {
      id = UUID.randomUUID().getMostSignificantBits();
      spitzen = command.getSpitzen();
      suit = command.getSuit();
      hand = command.isHand();
      schneider = command.isSchneider();
      schwarz = command.isSchwarz();
      ouvert = command.isOuvert();
      isWon = command.isWon();
      schneiderAnnounced = command.isSchneiderAnnounced();
      schwarzAnnounced = command.isSchwarzAnnounced();
      gameId = command.getGameId();
      playerPosition = command.getPlayerPosition();
      index = command.getIndex();
      isPasse = command.isPasse();
   }

   @Override
   public int toPoints() {
      if (isPasse) {
         return 0;
      }
      if (suit == SkatSuit.NULL) {
         return SkatScoreToPointsConverter.convertNullScore(this);
      }
      if (suit == SkatSuit.GRAND) {
         return SkatScoreToPointsConverter.convertGrandScore(this);
      }
      return SkatScoreToPointsConverter.convertSuitScore(this);
   }

   public int getSpitzen() {
      return spitzen;
   }

   public SkatSuit getSuit() {
      return suit;
   }

   public boolean isHand() {
      return hand;
   }

   public boolean isSchneider() {
      return schneider;
   }

   public boolean isSchwarz() {
      return schwarz;
   }

   public boolean isOuvert() {
      return ouvert;
   }

   public boolean isWon() {
      return isWon;
   }

   public boolean isSchneiderAnnounced() {
      return schneiderAnnounced;
   }

   public boolean isSchwarzAnnounced() {
      return schwarzAnnounced;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public boolean isPasse() {
      return isPasse;
   }
}
