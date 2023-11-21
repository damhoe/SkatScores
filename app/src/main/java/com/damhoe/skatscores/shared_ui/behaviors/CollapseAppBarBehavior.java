package com.damhoe.skatscores.shared_ui.behaviors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

public class CollapseAppBarBehavior extends AppBarLayout.Behavior {
   private boolean shouldCollapse = true;

   public CollapseAppBarBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public void setShouldCollapse(boolean shouldCollapse) {
      this.shouldCollapse = shouldCollapse;
   }

   @Override
   public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull AppBarLayout child, @NonNull MotionEvent ev) {
      if (!shouldCollapse) {
         return false;
      }
      return super.onInterceptTouchEvent(parent, child, ev);
   }

   @Override
   public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull AppBarLayout child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
      if (!shouldCollapse) {
         return false;
      }
      return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
   }

   @Override
   public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull AppBarLayout child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
      if (!shouldCollapse) {
         return;
      }
      super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
   }
}
