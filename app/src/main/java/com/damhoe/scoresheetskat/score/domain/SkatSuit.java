package com.damhoe.scoresheetskat.score.domain;

public enum SkatSuit {
 GRAND,
 NULL,
 CLUBS,
 SPADES,
 HEARTS,
 DIAMONDS,
 INVALID;

 public int asInteger() {
  return ordinal();
 }

 public static SkatSuit fromInteger(int value) {
  for (SkatSuit suit: SkatSuit.values()) {
   if (suit.ordinal() == value) {
    return suit;
   }
  }
  return SkatSuit.CLUBS; // default
 }
}
