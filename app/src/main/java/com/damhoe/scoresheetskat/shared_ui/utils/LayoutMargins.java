package com.damhoe.scoresheetskat.shared_ui.utils;

public class LayoutMargins {
   private int leftMargin;
   private int topMargin;
   private int rightMargin;
   private int bottomMargin;

   public LayoutMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
      this.leftMargin = leftMargin;
      this.topMargin = topMargin;
      this.rightMargin = rightMargin;
      this.bottomMargin = bottomMargin;
   }

   public int getRightMargin() {
      return rightMargin;
   }

   public void setRightMargin(int rightMargin) {
      this.rightMargin = rightMargin;
   }

   public int getBottomMargin() {
      return bottomMargin;
   }

   public void setBottomMargin(int bottomMargin) {
      this.bottomMargin = bottomMargin;
   }

   public int getTopMargin() {
      return topMargin;
   }

   public void setTopMargin(int topMargin) {
      this.topMargin = topMargin;
   }

   public int getLeftMargin() {
      return leftMargin;
   }

   public void setLeftMargin(int leftMargin) {
      this.leftMargin = leftMargin;
   }
}
