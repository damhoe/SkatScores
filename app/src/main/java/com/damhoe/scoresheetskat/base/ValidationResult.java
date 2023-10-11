package com.damhoe.scoresheetskat.base;

public class ValidationResult {
   private boolean isValid;
   private String message;

   public boolean isValid() {
      return isValid;
   }

   public boolean failed() {
      return !isValid;
   }

   ValidationResult(boolean isValid, String message) {
      this.isValid = isValid;
      this.message = message;
   }

   public static ValidationResult valid() {
      return new ValidationResult(true, null);
   }

   public static ValidationResult failed(String message) {
      return new ValidationResult(false, message);
   }

   public String getMessage() {
      return message;
   }
}
