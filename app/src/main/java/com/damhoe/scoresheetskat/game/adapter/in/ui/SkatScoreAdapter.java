package com.damhoe.scoresheetskat.game.adapter.in.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.ArrayList;
import java.util.List;

public class SkatScoreAdapter extends RecyclerView.Adapter<SkatScoreAdapter.SkatScoreViewHolder> {
   private final IScoreActionListener mListener;
   private final List<SkatScore> mScores;
   private int numberOfRounds;

   public SkatScoreAdapter(IScoreActionListener listener) {
      mScores = new ArrayList<>();
      mListener = listener;
      numberOfRounds = 0;
   }

   public void setScores(List<SkatScore> newScores) {
      if (newScores != null) {
         SkatScoreDiffCallback callback = new SkatScoreDiffCallback(mScores, newScores);
         DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
         mScores.clear();
         mScores.addAll(newScores);
         diffResult.dispatchUpdatesTo(this);
      }
   }

   public List<SkatScore> getScores() {
      return mScores;
   }

   public int getPosition(long scoreId) {
      for (int i = 0; i < mScores.size(); i++) {
         if (mScores.get(i).getId() == scoreId) {
            return i;
         }
      }
      return -1;
   }

   public void setNumberOfRounds(int newNumberOfRounds) {
      numberOfRounds = newNumberOfRounds;
   }

   @NonNull
   @Override
   public SkatScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.points_board_item_v2, parent, false);
      return new SkatScoreViewHolder(itemView);
   }

   @Override
   public void onBindViewHolder(@NonNull SkatScoreViewHolder holder, int position) {
      holder.roundsText.setText(String.valueOf(position + 1));

      if (position >= mScores.size()) {
         return;
      }
      SkatScore score = mScores.get(position);
      if (score == null) {
         return;
      }

      int[] pointsArray = new int[]{0, 0, 0}; // Initialize with zeros
      if (score.getPlayerPosition() >= 0) {
         // Set the value at the appropriate index
         pointsArray[score.getPlayerPosition()] = score.toPoints();
      }
      holder.points1Text.setText(makeScoreString(pointsArray[0]));
      holder.points2Text.setText(makeScoreString(pointsArray[1]));
      holder.points3Text.setText(makeScoreString(pointsArray[2]));

      holder.itemView.setOnLongClickListener(view -> {
         showPopupMenu(view.getContext(), view, position);
         Log.d("Score Event", "Item clicked at position " + position);
         return true;
      });
   }

   private String makeScoreString(int points) {
      if (points == 0) {
         return "-";
      }
      return String.valueOf(points);
   }

   @Override
   public void onBindViewHolder(@NonNull SkatScoreViewHolder holder,
                                int position,
                                @NonNull List<Object> payloads) {
      if (payloads.isEmpty()) {
         // Full binding if payloads are empty
         onBindViewHolder(holder, position);
      } else {
         // Handle payloads (e.g., update item index)
         for (Object payload : payloads) {
            if (payload.equals("payload")) {
               // Update the item index view
               holder.updateRounds(position);
            }
         }
      }
   }

   @SuppressLint("NonConstantResourceId")
   private void showPopupMenu(Context context, View anchorView, int position) {
      PopupMenu menu = new PopupMenu(context, anchorView, Gravity.END);
      menu.getMenuInflater().inflate(R.menu.score_item_menu, menu.getMenu());
      if (position != mScores.size() - 1) {
         menu.getMenu().removeItem(R.id.delete);
      }
      menu.setForceShowIcon(true);

      menu.setOnMenuItemClickListener(item -> {
         switch (item.getItemId()) {
            case R.id.delete:
               Log.d("Menu item clicked", "Delete menu item.");
               mListener.notifyDelete();
               return true;
            case R.id.details:
               Log.d("Menu item clicked", "Show item details.");
               mListener.notifyDetails(mScores.get(position));
               return true;
            case R.id.edit:
               Log.d("Menu item clicked", "Delete menu item");
               mListener.notifyEdit(mScores.get(position));
               return true;
            default:
               return false;
         }
      });

      // Show popup menu
      menu.show();
   }

   @Override
   public int getItemCount() {
      return mScores.size();
   }


   public static class SkatScoreViewHolder extends RecyclerView.ViewHolder {
      public TextView roundsText;
      public TextView points1Text;
      public TextView points2Text;
      public TextView points3Text;
      public TextView points4Text;

      public SkatScoreViewHolder(@NonNull View itemView) {
         super(itemView);
         points1Text = itemView.findViewById(R.id.points1_text);
         points2Text = itemView.findViewById(R.id.points2_text);
         points3Text = itemView.findViewById(R.id.points3_text);
         roundsText = itemView.findViewById(R.id.round_text);
      }

      public void updateRounds(int position) {
         roundsText.setText(String.valueOf(position));
      }
   }

   public static class ItemDecoration extends RecyclerView.ItemDecoration {

      @Override
      public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull RecyclerView.State state
      ) {
         outRect.top = 1;
      }
   }

   public static class SkatScoreDiffCallback extends DiffUtil.Callback {

      private final List<SkatScore> mOldScores;
      private final List<SkatScore> mNewScores;

      public SkatScoreDiffCallback(List<SkatScore> oldScores, List<SkatScore> newScores) {
         mNewScores = newScores;
         mOldScores = oldScores;
      }

      @Override
      public int getOldListSize() {
         return mOldScores.size();
      }

      @Override
      public int getNewListSize() {
         return mNewScores.size();
      }

      @Override
      public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
         return mOldScores.get(oldItemPosition).equals(mNewScores.get(newItemPosition));
      }

      @Override
      public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
         return mOldScores.get(oldItemPosition).getId() == mNewScores.get(newItemPosition).getId();
      }

      @Override
      public Object getChangePayload(int oldItemPosition, int newItemPosition) {
         // Check if a payload was requested
         return null;
      }
   }
}
