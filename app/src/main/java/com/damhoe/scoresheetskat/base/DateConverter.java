package com.damhoe.scoresheetskat.base;

import androidx.appcompat.app.AppCompatDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

   public static String toAppLocaleString(Date data) {
      Locale appLocale = AppCompatDelegate.getApplicationLocales().get(0);
      if (appLocale == null) {
         appLocale = Locale.getDefault();
      }
      return toLocaleString(data, appLocale);
   }

   public static String toAppLocaleStringFullMonth(Date data) {
      Locale appLocale = AppCompatDelegate.getApplicationLocales().get(0);
      if (appLocale == null) {
         appLocale = Locale.getDefault();
      }
      return toLocaleStringFullMonth(data, appLocale);
   }

   public static String toLocaleString(Date date, Locale locale) {
      String pattern;
      if (locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
         pattern = "d. MMM yyyy";
      } else {
         pattern = "MMM d, yyyy";
      }
      return new SimpleDateFormat(pattern, locale).format(date);
   }

   public static String toLocaleStringFullMonth(Date date, Locale locale) {
      String pattern;
      if (locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
         pattern = "d. MMMM";
      } else {
         pattern = "MMMM d";
      }
      return new SimpleDateFormat(pattern, locale).format(date);
   }
}
