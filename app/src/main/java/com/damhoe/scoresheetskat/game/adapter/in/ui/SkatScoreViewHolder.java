package com.damhoe.scoresheetskat.game.adapter.in.ui;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damhoe.scoresheetskat.R;
import com.google.android.material.card.MaterialCardView;

public class SkatScoreViewHolder extends RecyclerView.ViewHolder {
   public TextView roundsText;
   public TextView pointsText;
   public TextView points1Text;
   public TextView points2Text;
   public TextView points3Text;
   public TextView points4Text;

   public SkatScoreViewHolder(@NonNull View itemView) {
      super(itemView);
      pointsText = itemView.findViewById(R.id.game_points_text);
      points1Text = itemView.findViewById(R.id.points1_text);
      points2Text = itemView.findViewById(R.id.points2_text);
      points3Text = itemView.findViewById(R.id.points3_text);
      roundsText = itemView.findViewById(R.id.round_text);
   }

   public void updateRounds(int position) {
      roundsText.setText(String.valueOf(position));
   }
}
