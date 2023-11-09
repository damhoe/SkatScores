package com.damhoe.scoresheetskat.score.domain;

import com.damhoe.scoresheetskat.score.Constant;
import com.damhoe.scoresheetskat.score.application.SkatScoreToPointsConverter;

import java.util.UUID;

public class SkatScore extends Score {
   private final int spitzen; // negative value if missing
   private final SkatSuit suit; // Clubs, Diamonds, Hears, Spades, Null, Grand
   private final SkatResult result; // Won, Lost, Overbid, Passe
   private final boolean hand;
   private final boolean schneider;
   private final boolean schneiderAnnounced;
   private final boolean schwarz;
   private final boolean schwarzAnnounced;
   private final boolean ouvert;

   public SkatScore(SkatScoreCommand command) {
      id = UUID.randomUUID().getMostSignificantBits();
      spitzen = command.getSpitzen();
      suit = command.getSuit();
      hand = command.isHand();
      schneider = command.isSchneider();
      schwarz = command.isSchwarz();
      ouvert = command.isOuvert();
      result = command.getResult();
      schneiderAnnounced = command.isSchneiderAnnounced();
      schwarzAnnounced = command.isSchwarzAnnounced();
      gameId = command.getGameId();
      playerPosition = command.getPlayerPosition();
   }

   @Override
   public int toPoints() {
      if (isPasse()) {
         return 0;
      }
      if (isOverbid()) {
         return -50;
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

   public SkatResult getResult() {
      return result;
   }

   public boolean isWon() {
      return SkatResult.WON.equals(result);
   }

   public boolean isSchneiderAnnounced() {
      return schneiderAnnounced;
   }

   public boolean isSchwarzAnnounced() {
      return schwarzAnnounced;
   }

   public boolean isPasse() {
      return playerPosition == Constant.PASSE_PLAYER_POSITION;
   }

   public boolean isOverbid() { return SkatResult.OVERBID.equals(getResult()); }
}
