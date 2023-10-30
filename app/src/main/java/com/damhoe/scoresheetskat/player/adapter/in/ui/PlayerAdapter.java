package com.damhoe.scoresheetskat.player.adapter.in.ui;

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
import com.damhoe.scoresheetskat.player.domain.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private final NotifyItemClickListener listener;
    private List<Player> players;

    public PlayerAdapter(NotifyItemClickListener listener) {
        this.listener = listener;
        players = new ArrayList<>();
    }

    void setPlayers(List<Player> newPlayers) {
        if (players != null) {
            PlayerDiffCallback diffCallback = new PlayerDiffCallback(players, newPlayers);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            players.clear();
            players.addAll(newPlayers);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public PlayerAdapter.PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerAdapter.PlayerViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Player player = players.get(position);
        holder.name.setText(player.getName());
        holder.createdAt.setText(new SimpleDateFormat("dd. MMM yyyy", Locale.getDefault()).format(player.getCreatedAt()));
        holder.numberGames.setText(String.format("%d Games", player.getGameCount()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.notifyItemClick(player, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static final class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView numberGames;
        TextView createdAt;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            numberGames = itemView.findViewById(R.id.number_games);
            createdAt = itemView.findViewById(R.id.date);
        }
    }

    static class PlayerDiffCallback extends DiffUtil.Callback {

        private final List<Player> oldPlayers, newPlayers;

        public PlayerDiffCallback(List<Player> oldPlayers, List<Player> newPlayers) {
            this.oldPlayers = oldPlayers;
            this.newPlayers = newPlayers;
        }

        @Override
        public int getOldListSize() {
            return oldPlayers.size();
        }

        @Override
        public int getNewListSize() {
            return newPlayers.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldPlayers.get(oldItemPosition).getID() == newPlayers.get(newItemPosition).getID();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldPlayers.get(oldItemPosition).equals(newPlayers.get(newItemPosition));
        }
    }

    static class ItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.top = 4;
            outRect.bottom = 4;
            //super.getItemOffsets(outRect, view, parent, state);
        }
    }
}
