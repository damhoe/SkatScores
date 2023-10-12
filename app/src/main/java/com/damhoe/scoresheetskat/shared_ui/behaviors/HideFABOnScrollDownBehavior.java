package com.damhoe.scoresheetskat.shared_ui.behaviors;

import android.os.Handler;
import android.view.View;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class HideFABOnScrollDownBehavior {

   ExtendedFloatingActionButton mFAB;

   public HideFABOnScrollDownBehavior(ExtendedFloatingActionButton fab) {
      mFAB = fab;
   }

   public View.OnScrollChangeListener getOnScrollChangeListener() {
      return new View.OnScrollChangeListener() {
         @Override
         public void onScrollChange(View view, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (mFAB == null) {
               return;
            }
            if (scrollY - oldScrollY > 10 && mFAB.isShown()) {
               mFAB.hide();
               new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                     if (!mFAB.isShown()) {
                        mFAB.show();
                     }
                  }
               }, 500);
            }
            if (scrollY < oldScrollY - 10 && !mFAB.isShown()) {
               mFAB.show();
            }

            if (scrollY == 0) {
               mFAB.show();
            }
         }
      };
   }
}
