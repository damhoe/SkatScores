package com.damhoe.scoresheetskat.shared_ui.behaviors;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScrollViewBehaviorHandler {

   @SuppressLint("ClickableViewAccessibility")
   public static void setupWithFAB(
           @NonNull NestedScrollView nestedScrollView,
           @NonNull FloatingActionButton button
   ) {
      int tolerance = 12;
      int delayInMillis = 600;

      Handler handler = new Handler();

      View.OnScrollChangeListener listener = new View.OnScrollChangeListener() {
         @Override
         public void onScrollChange(View view, int scrollX, int scrollY,
                                    int oldScrollX, int oldScrollY) {
            int dScrollY = scrollY - oldScrollY;

            // On scroll down
            if (dScrollY > tolerance && button.isShown()) {
               button.hide();
            }

            // On scroll up
            if (dScrollY < -tolerance && !button.isShown()) {
               button.show();
            }

            // On scrolled up
            if (scrollY == 0 && !button.isShown()) {
               button.show();
            }

            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  if (!button.isShown()) {
                     button.show();
                  }
               }
            }, delayInMillis);
         }
      };

      nestedScrollView.setOnScrollChangeListener(listener);
   }

   @SuppressLint("ClickableViewAccessibility")
   public static void setupWithExtendedFAB(
           @NonNull NestedScrollView nestedScrollView,
           @NonNull ExtendedFloatingActionButton button
   ) {
      int tolerance = 12;
      int delayInMillis = 600;

      Handler handler = new Handler();

      View.OnScrollChangeListener listener = new View.OnScrollChangeListener() {
         @Override
         public void onScrollChange(View view, int scrollX, int scrollY,
                                    int oldScrollX, int oldScrollY) {
            int dScrollY = scrollY - oldScrollY;

            // On scroll down
            if (dScrollY > tolerance && button.isShown()) {
               button.hide();
            }

            // On scroll up
            if (dScrollY < -tolerance && !button.isShown()) {
               button.show();
            }

            // On scrolled up
            if (scrollY == 0 && !button.isShown()) {
               button.show();
            }

            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
               @Override
               public void run() {
                  if (!button.isShown()) {
                     button.show();
                  }
               }
            }, delayInMillis);
         }
      };

      nestedScrollView.setOnScrollChangeListener(listener);
   }
}
