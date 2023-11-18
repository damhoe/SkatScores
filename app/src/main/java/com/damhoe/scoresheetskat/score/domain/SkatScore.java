package com.damhoe.scoresheetskat.score.domain;

import android.content.Context;

import androidx.annotation.NonNull;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.score.Constant;
import com.damhoe.scoresheetskat.score.application.SkatScoreToPointsConverter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

   public static class TextMaker {
      private String text = "";
      private final Context mContext;
      private SkatScore mScore;

      private Map<SkatSuit, Integer> suitTextResourceIdMap = new HashMap<>();

      public TextMaker(Context context) {
         mContext = context;
         suitTextResourceIdMap.put(SkatSuit.NULL, R.string.label_null);
         suitTextResourceIdMap.put(SkatSuit.HEARTS, R.string.description_hearts);
         suitTextResourceIdMap.put(SkatSuit.GRAND, R.string.label_grand);
         suitTextResourceIdMap.put(SkatSuit.CLUBS, R.string.description_clubs);
         suitTextResourceIdMap.put(SkatSuit.SPADES, R.string.description_spades);
         suitTextResourceIdMap.put(SkatSuit.DIAMONDS, R.string.description_diamonds);
         suitTextResourceIdMap.put(SkatSuit.INVALID, R.string.unknown);
      }

      public TextMaker setupWithSkatScore(SkatScore score) {
         mScore = score;
         return this;
      }

      public String make() {
         if (mScore.isPasse()) {
            return makePasseText();
         }

         if (mScore.isOverbid()) {
            return makeOverbidText();
         }

         addResultText();
         addSpielText();
         addAnnouncements();
         boolean isWritten;
         isWritten = addSchneiderSchwarz(false);
         isWritten = addHand(isWritten);
         isWritten = addOuvert(isWritten);

         return text;
      }

      private String makePasseText() {
         return mContext.getString(R.string.passe_text);
      }

      private String makeOverbidText() {
         return String.format(Locale.getDefault(), mContext.getString(R.string.overbid_text));
      }

      private void addResultText() {
         text += mContext.getString(mScore.isWon() ? R.string.won_text : R.string.lost_text);
         addEmptyLine();
      }

      /** @noinspection DataFlowIssue*/
      private void addSpielText() {
         if (SkatSuit.NULL == mScore.getSuit()) {
            text += mContext.getString(suitTextResourceIdMap.get(mScore.getSuit()));
            addEmptyLine();
            return;
         }

         text += mContext.getString(suitTextResourceIdMap.get(mScore.getSuit()));
         addSpaces();
         text += mContext.getString(R.string.title_spitzen);
         addSpaces();
         text += String.valueOf(mScore.getSpitzen());
         addEmptyLine();
      }

      /** @noinspection SameParameterValue*/
      private boolean addSchneiderSchwarz(boolean isBehindText) {
         if (mScore.isSchwarz()) {
            if (isBehindText) {
               addComma();
            }
            text += mContext.getString(R.string.label_schwarz);
            return true;
         }

         if (mScore.isSchneider()) {
            if (isBehindText) {
               addComma();
            }
            text += mContext.getString(R.string.label_schneider);
            return true;
         }

         return false;
      }

      private void addAnnouncements() {
         if (mScore.isSchwarzAnnounced()) {
            text += mContext.getString(R.string.label_schwarz_announced);
            addEmptyLine();
            return;
         }

         if (mScore.isSchneiderAnnounced()) {
            text += mContext.getString(R.string.label_schneider_announced);
            addEmptyLine();
         }
      }

      private boolean addHand(boolean isBehindText) {
         if (mScore.isHand()) {
            if (isBehindText) {
               addComma();
            }
            text += mContext.getString(R.string.label_hand);
            return true;
         }
         return false;
      }

      private boolean addOuvert(boolean isBehindText) {
         if (mScore.isOuvert()) {
            if (isBehindText) {
               addComma();
            }
            text += mContext.getString(R.string.label_ouvert);
            return true;
         }
         return false;
      }

      private void addComma() {
         text += ", ";
      }

      private void addSpaces() {
         text += "\t\t";
      }

      private void addEmptyLine() {
         text += "\n\n";
      }
   }
}
