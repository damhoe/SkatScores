package com.damhoe.scoresheetskat.persistance;

import android.database.Cursor;

public final class Utility {

   public static long getLongFromCursor(Cursor cursor, String columnName, long defaultValue) {
      int columnIndex = cursor.getColumnIndex(columnName);
      return (columnIndex != -1) ? cursor.getLong(columnIndex) : defaultValue;
   }

   public static String getStringFromCursor(Cursor cursor, String columnName, String defaultValue) {
      int columnIndex = cursor.getColumnIndex(columnName);
      return (columnIndex != -1) ? cursor.getString(columnIndex) : defaultValue;
   }

   public static int getIntFromCursor(Cursor cursor, String columnName, int defaultValue) {
      int columnIndex = cursor.getColumnIndex(columnName);
      return (columnIndex != -1) ? cursor.getInt(columnIndex) : defaultValue;
   }
}
