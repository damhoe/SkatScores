package com.damhoe.scoresheetskat.game.adapter.in.ui.shared;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GamePreviewAdapter extends
        RecyclerView.Adapter<GamePreviewAdapter.GamePreviewViewHolder> {

   private final GamePreviewItemClickListener mPreviewClickListener;
   private final List<SkatGamePreview> mPreviews;

    public GamePreviewAdapter(GamePreviewItemClickListener listener) {
      mPreviewClickListener = listener;
      mPreviews = new ArrayList<>();
   }

   public void setGamePreviews(List<SkatGamePreview> newPreviews) {
       if (newPreviews != null) {
          GamePreviewDiffCallback callback =
                  new GamePreviewDiffCallback(this.mPreviews, newPreviews);
          DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
          mPreviews.clear();
          mPreviews.addAll(newPreviews);
          diffResult.dispatchUpdatesTo(this);
       }
   }

   @NonNull
   @Override
   public GamePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new GamePreviewViewHolder(
              LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.item_game_preview, parent, false)
      );
   }

   @SuppressLint("DefaultLocale")
   @Override
   public void onBindViewHolder(@NonNull GamePreviewViewHolder holder, int position) {
      SkatGamePreview preview = mPreviews.get(position);
      holder.title.setText(preview.getTitle());
      holder.playerNames.setText(String.join(", ", preview.getPlayerNames()));
      holder.date.setText(
              new SimpleDateFormat("LLL, d yyyy", Locale.getDefault())
                      .format(preview.getDate()));

      if (preview.getGameRunStateInfo().isFinished()) {
         holder.round.setText("Finished");
      } else {
         int currentRound = preview.getGameRunStateInfo().getCurrentRound();
         int roundsCount = preview.getGameRunStateInfo().getRoundsCount();
         holder.round.setText(String.format("%d/%d", currentRound, roundsCount));
      }
      holder.buttonContinue.setOnClickListener(view -> mPreviewClickListener.notifySelect(preview));
      holder.buttonDelete.setOnClickListener(view -> mPreviewClickListener.notifyDelete(preview));
   }

   @Override
   public int getItemCount() {
      return mPreviews.size();
   }

   public static final class GamePreviewViewHolder extends RecyclerView.ViewHolder {
      TextView title;
      TextView playerNames;
      TextView round;
      TextView date;
      MaterialButton buttonContinue;
      MaterialButton buttonDelete;

      public GamePreviewViewHolder(@NonNull View item) {
         super(item);
         title = (TextView) item.findViewById(R.id.title);
         playerNames = (TextView) item.findViewById(R.id.players);
         round = (TextView) item.findViewById(R.id.rounds);
         date = (TextView) item.findViewById(R.id.date);
         buttonContinue = (MaterialButton) item.findViewById(R.id.button_continue);
         buttonDelete = (MaterialButton) item.findViewById(R.id.button_delete);
      }
   }

   public static final class ItemDecoration extends RecyclerView.ItemDecoration {
       private final int offset;

       public ItemDecoration(int offset) {
          this.offset = offset;
       }

      @Override
      public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
         outRect.top = offset;
         outRect.bottom = offset;
      }
   }

   public static class GamePreviewDiffCallback extends DiffUtil.Callback {

      private final List<SkatGamePreview> oldPreviews;
      private final List<SkatGamePreview> newPreviews;

      public GamePreviewDiffCallback(
              List<SkatGamePreview> oldPreviews,
              List<SkatGamePreview> newPreviews
      ) {
         this.oldPreviews = oldPreviews;
         this.newPreviews = newPreviews;
      }

      @Override
      public int getOldListSize() {
         return oldPreviews.size();
      }

      @Override
      public int getNewListSize() {
         return newPreviews.size();
      }

      @Override
      public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
         return oldPreviews.get(oldItemPosition) == newPreviews.get(newItemPosition);
      }

      @Override
      public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
         return oldPreviews.get(oldItemPosition).getGameId() == newPreviews.get(newItemPosition).getGameId();
      }
   }
}
