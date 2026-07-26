package com.damhoe.skatscores.player.adapter.presentation

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R

class PlayerAdapter(private val listener: NotifyItemClickListener) :
    ListAdapter<PlayerInfo, PlayerAdapter.PlayerViewHolder>(PlayerDiffCallback())
{
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): PlayerViewHolder
    {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_player,
                parent,
                false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlayerViewHolder,
        position: Int,
    )
    {
        val playerInfo = getItem(position)
        holder.name.text = playerInfo.name.value
        holder.numberGames.text = String.format(
            holder.itemView.context.getString(R.string.template_game_count_long),
            playerInfo.totalGamesPlayed
        )

        holder.itemView.setOnClickListener { view: View? ->
            listener.notifyItemClick(
                playerInfo.playerId,
                position
            )
        }
    }

    class PlayerViewHolder(
        itemView: View,
        val name: TextView = itemView.findViewById(R.id.name),
        val numberGames: TextView = itemView.findViewById(R.id.number_games),
    ) : RecyclerView.ViewHolder(itemView)

    class PlayerDiffCallback() : DiffUtil.ItemCallback<PlayerInfo>()
    {
        override fun areItemsTheSame(
            oldItem: PlayerInfo, newItem: PlayerInfo
        ): Boolean
        {
            return oldItem.playerId == newItem.playerId
        }

        override fun areContentsTheSame(
            oldItem: PlayerInfo, newItem: PlayerInfo
        ): Boolean
        {
            return oldItem == newItem
        }
    }

    class ItemDecoration : RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        )
        {
            outRect.top = 4
            outRect.bottom = 4
        }
    }
}