package com.damhoe.scoresheetskat.game.adapter.in.ui.shared;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.game.domain.GamePreview;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GamePreviewAdapter extends RecyclerView.Adapter<GamePreviewAdapter.GamePreviewViewHolder> {

   private final GamePreviewItemClickListener listener;
   private List<GamePreview> gamePreviews;

    public GamePreviewAdapter(GamePreviewItemClickListener listener) {
      this.listener = listener;
      gamePreviews = new ArrayList<>();
   }

   public void setGamePreviews(List<GamePreview> gamePreviews) {
      this.gamePreviews = gamePreviews;
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
      GamePreview preview = gamePreviews.get(position);
      holder.title.setText(preview.getTitle());
      holder.playerNames.setText(String.join(", ", preview.getPlayerNames()));
      holder.date.setText(
              new SimpleDateFormat("LLL, d yyyy", Locale.getDefault())
                      .format(preview.getDate()));
      holder.round.setText("11/12"); // TODO
      holder.buttonContinue.setOnClickListener(view -> listener.notifyItemClicked(preview));
   }

   @Override
   public int getItemCount() {
      return gamePreviews.size();
   }

   public static final class GamePreviewViewHolder extends RecyclerView.ViewHolder {
      TextView title;
      TextView playerNames;
      TextView round;
      TextView date;
      MaterialButton buttonContinue;

      public GamePreviewViewHolder(@NonNull View item) {
         super(item);
         title = (TextView) item.findViewById(R.id.title);
         playerNames = (TextView) item.findViewById(R.id.players);
         round = (TextView) item.findViewById(R.id.rounds);
         date = (TextView) item.findViewById(R.id.date);
         buttonContinue = (MaterialButton) item.findViewById(R.id.button_continue);
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
}
