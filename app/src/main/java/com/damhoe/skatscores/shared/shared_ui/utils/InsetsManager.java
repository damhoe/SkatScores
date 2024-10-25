package com.damhoe.skatscores.shared.shared_ui.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class InsetsManager {

   /**
    * Applies a top padding that matches the
    * status bar size to the provided view.
    * Should be used on colored app bar layouts.
    *
    * @param view View on which the insets are applied
    */
   public static void applyStatusBarInsets(@NonNull View view) {
      ViewCompat.setOnApplyWindowInsetsListener(view, (v, insetsCompat) -> {
         Insets insets = insetsCompat.getInsets(WindowInsetsCompat.Type.statusBars());

         Log.d("Status bar insets", "Top: " + insets.top);
         Log.d("Status bar insets", "Left: " + insets.left);
         Log.d("Status bar insets", "Right: " + insets.right);
         Log.d("Status bar insets", "Bottom: " + insets.bottom);


         view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
         return WindowInsetsCompat.CONSUMED;
      });
   }

   public static void setAppearanceDarkStatusBar(@NonNull Window window) {
      WindowInsetsControllerCompat windowInsetsController =
              WindowCompat.getInsetsController(window, window.getDecorView());
      windowInsetsController.setAppearanceLightStatusBars(false);
   }

   public static void setAppearanceLightStatusBar(@NonNull Window window) {
      WindowInsetsControllerCompat windowInsetsController =
              WindowCompat.getInsetsController(window, window.getDecorView());
      windowInsetsController.setAppearanceLightStatusBars(true);
   }

   /**
    * Applies a bottom margin that matches the
    * navigation bar size to the provided view.
    *
    * @param view View on which the insets are applied
    */
   public static void applyNavigationBarInsets(@NonNull View view) {
      applyNavigationBarInsets(view, null);
   }

   /**
    * Applies a bottom margin that matches the
    * navigation bar size to the provided view.
    *
    * @param view View on which the insets are applied
    * @param margins Default layout margins that are added to the inset margins.
    */
   public static void applyNavigationBarInsets(@NonNull View view, LayoutMargins margins) {
      if (margins == null) {
         margins = new LayoutMargins(0, 0, 0, 0);
      }

      final LayoutMargins mMargins = margins;

      ViewCompat.setOnApplyWindowInsetsListener(view, (v, insetsCompat) -> {
         Insets insets = insetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars());

         Log.d("Navigation bar insets", "Top: " + insets.top);
         Log.d("Navigation bar insets", "Left: " + insets.left);
         Log.d("Navigation bar insets", "Right: " + insets.right);
         Log.d("Navigation bar insets", "Bottom: " + insets.bottom);

         MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();

         params.leftMargin = insets.left + mMargins.getLeftMargin();
         params.rightMargin = insets.right + mMargins.getRightMargin();
         params.topMargin = insets.top + mMargins.getTopMargin();
         params.bottomMargin = insets.bottom + mMargins.getBottomMargin();

         view.setLayoutParams(params);

         return WindowInsetsCompat.CONSUMED;
      });
   }

   /**
    * Applies both navigation bar and status bar insets
    *
    * @param view View on which the insets are applied
    */
   public static void applySystemBarInsets(@NonNull View view) {
      ViewCompat.setOnApplyWindowInsetsListener(view, (v, insetsCompat) -> {
         Insets insets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());

         Log.d("Navigation bar insets", "Top: " + insets.top);
         Log.d("Navigation bar insets", "Left: " + insets.left);
         Log.d("Navigation bar insets", "Right: " + insets.right);
         Log.d("Navigation bar insets", "Bottom: " + insets.bottom);

         MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();

         params.leftMargin = insets.left;
         params.rightMargin = insets.right;
         params.bottomMargin = insets.bottom;

         view.setLayoutParams(params);
         view.setPadding(view.getPaddingLeft(), insets.top, view.getPaddingRight(), view.getPaddingBottom());

         return WindowInsetsCompat.CONSUMED;
      });
   }
}
