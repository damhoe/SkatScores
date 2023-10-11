package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.recyclerview.widget.DiffUtil;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;

public class SkatScoreDiffCallback extends DiffUtil.Callback {
   private final List<SkatScore> oldScores;
   private final List<SkatScore> newScores;

   public SkatScoreDiffCallback(List<SkatScore> oldScores, List<SkatScore> newScores) {
      this.newScores = newScores;
      this.oldScores = oldScores;
   }

   @Override
   public int getOldListSize() {
      return oldScores.size();
   }

   @Override
   public int getNewListSize() {
      return newScores.size();
   }

   @Override
   public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      return oldScores.get(oldItemPosition) == newScores.get(newItemPosition);
   }

   @Override
   public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return oldScores.get(oldItemPosition).equals(newScores.get(newItemPosition));
   }

   @Override
   public Object getChangePayload(int oldItemPosition, int newItemPosition) {
      // Check if a payload was requested
      if (oldScores.get(oldItemPosition) != newScores.get(newItemPosition)) {
         // The IDs are different, indicating a change in item index
         // Return a payload that indicates the position change
         return "payload";
      }
      return null;
   }
}
