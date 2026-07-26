package com.damhoe.skatscores.library

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.library.GamePreviewItemClickListener
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.library.GamePreviewAdapter.*
import com.google.android.material.button.MaterialButton
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class GamePreviewAdapter(private val itemClickListener: GamePreviewItemClickListener) :
    ListAdapter<SkatGamePreview, GamePreviewViewHolder>(GamePreviewDiffCallback())
{
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): GamePreviewViewHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_game_preview, parent, false
        )
        return GamePreviewViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: GamePreviewViewHolder, position: Int
    )
    {
        val preview = getItem(position)
        holder.title.text = preview.title.value

        val systemZoneId: ZoneId = ZoneId.systemDefault()
        val skeleton = "EEEEMMMd"
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            skeleton
        )
        val localizedFormatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        holder.date.text = preview.playedAt
            .atZone(systemZoneId)
            .format(localizedFormatter)

        holder.buttonContinue.setOnClickListener {
            itemClickListener.notifySelect(preview)
        }
        holder.buttonDelete.setOnClickListener {
            itemClickListener.notifyDelete(preview)
        }
    }

    class GamePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var title: TextView = itemView.findViewById(R.id.title)
        var playerNames: TextView = itemView.findViewById(R.id.players)
        var round: TextView = itemView.findViewById(R.id.rounds)
        var date: TextView = itemView.findViewById(R.id.date)
        var buttonContinue: MaterialButton = itemView.findViewById(R.id.button_continue)
        var buttonDelete: MaterialButton = itemView.findViewById(R.id.button_delete)
    }

    class ItemDecoration(private val offset: Int) : RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        )
        {
            outRect.top = offset
            outRect.bottom = offset
        }
    }

    class GamePreviewDiffCallback() : DiffUtil.ItemCallback<SkatGamePreview>()
    {
        override fun areItemsTheSame(
            oldItem: SkatGamePreview, newItem: SkatGamePreview
        ): Boolean
        {
            return oldItem.gameId == newItem.gameId
        }

        override fun areContentsTheSame(
            oldItem: SkatGamePreview, newItem: SkatGamePreview
        ): Boolean
        {
            return oldItem == newItem
        }
    }
}