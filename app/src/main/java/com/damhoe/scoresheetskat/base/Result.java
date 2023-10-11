package com.damhoe.scoresheetskat.base;

public class Result<T> {
   private final T value;
   private final String message;

   public Result(T value, String errorMessage) {
      this.value = value;
      this.message = errorMessage;
   }

   public static <T> Result<T> success(T value) {
      return new Result<>(value, null);
   }

   public static <T> Result<T> failure(String message) {
      return new Result<>(null, message);
   }

   public boolean isSuccess() {
      return value != null;
   }

   public boolean isFailure() {
      return value == null;
   }

   public T getValue() {
      return value;
   }

   public String getMessage() {
      return message;
   }
}
