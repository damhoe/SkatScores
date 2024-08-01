package com.damhoe.skatscores.game.adapter.presentation.shared;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.damhoe.skatscores.R;
import com.damhoe.skatscores.base.DateConverter;
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;
import com.google.android.material.button.MaterialButton;

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

   @Override
   public void onBindViewHolder(@NonNull GamePreviewViewHolder holder, int position) {
      SkatGamePreview preview = mPreviews.get(position);
      holder.title.setText(preview.getTitle());
      holder.playerNames.setText(
              String.join(
                      " \n",
                      preview.getPlayerNames()));
      holder.date.setText(DateConverter.toAppLocaleStringFullMonth(preview.getDate()));

      if (preview.getGameRunStateInfo().isFinished()) {
         holder.round.setText("finished"); // R.string.label_finished
      } else {
         int currentRound = preview.getGameRunStateInfo().getCurrentRound();
         int roundsCount = preview.getGameRunStateInfo().getRoundsCount();
         holder.round.setText(String.format(Locale.getDefault(),
                 "%d/%d", currentRound, roundsCount));
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
         title = item.findViewById(R.id.title);
         playerNames = item.findViewById(R.id.players);
         round = item.findViewById(R.id.rounds);
         date = item.findViewById(R.id.date);
         buttonContinue = item.findViewById(R.id.button_continue);
         buttonDelete = item.findViewById(R.id.button_delete);
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
