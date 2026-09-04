package com.damhoe.skatscores.base;

import androidx.appcompat.app.AppCompatDelegate;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    public static String toAppLocaleString(Instant data) {
        Locale appLocale = AppCompatDelegate.getApplicationLocales().get(0);
        if (appLocale == null) {
            appLocale = Locale.getDefault();
        }
        return toLocaleString(data.atZone(ZoneId.systemDefault()).toLocalDate(), appLocale);
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

    public static String toLocaleString(LocalDate date, Locale locale) {
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
         pattern = "d. MMMM \nyyyy";
      } else {
         pattern = "MMMM d \nyyyy";
      }
      return new SimpleDateFormat(pattern, locale).format(date);
   }
}
