package com.damhoe.scoresheetskat.score.domain;

public enum SkatResult {
 WON,
 LOST,
 OVERBID,
 PASSE;

 public int asInteger() {
  return ordinal();
 }

 public static SkatResult fromInteger(int value) {
  for (SkatResult result: SkatResult.values()) {
   if (result.ordinal() == value) {
    return result;
   }
  }
  return SkatResult.PASSE; // default
 }
}