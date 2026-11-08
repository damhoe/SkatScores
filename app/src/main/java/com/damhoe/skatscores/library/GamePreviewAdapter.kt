package com.damhoe.skatscores.library

import com.damhoe.skatscores.shared.signed
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.ListPreview
import com.damhoe.skatscores.library.GamePreviewAdapter.GamePreviewViewHolder
import com.damhoe.skatscores.shared.asListDate

class GamePreviewAdapter(private val itemClickListener: GamePreviewItemClickListener) :
    ListAdapter<ListPreview, GamePreviewViewHolder>(GamePreviewDiffCallback())
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
        val context = holder.itemView.context

        holder.rounds.text = preview.totalRounds.toString()
        holder.title.text = preview.title.value
        holder.meta.text = context.getString(
            R.string.format_list_meta,
            preview.playedAt.asListDate(),
            preview.playerNames.joinToString(", ")
        )

        holder.winnerDelta.text = preview.leaderTotal?.let { signed(it) }.orEmpty()

        holder.itemView.setOnClickListener { itemClickListener.notifySelect(preview) }

        holder.itemView.setOnLongClickListener {
            itemClickListener.notifyDelete(preview)
            true
        }
    }

    class GamePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val rounds: TextView = itemView.findViewById(R.id.rounds)
        val title: TextView = itemView.findViewById(R.id.title)
        val meta: TextView = itemView.findViewById(R.id.meta)
        val winnerDelta: TextView = itemView.findViewById(R.id.winnerDelta)
    }

    /** [inset]: the row's side gutter in pixels, so the line stops where the text does. */
    class DividerDecoration(
        private val color: Int,
        private val inset: Int = 0,
    ) : RecyclerView.ItemDecoration()
    {
        private val paint = android.graphics.Paint().apply { this.color = this@DividerDecoration.color }

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        )
        {
            if (parent.getChildAdapterPosition(view) > 0)
            {
                outRect.top = 1
            }
        }

        override fun onDraw(
            canvas: android.graphics.Canvas, parent: RecyclerView, state: RecyclerView.State
        )
        {
            for (index in 1 until parent.childCount)
            {
                val child = parent.getChildAt(index)
                val top = (child.top - 1).toFloat()
                canvas.drawRect(
                    (parent.paddingLeft + inset).toFloat(),
                    top,
                    (parent.width - parent.paddingRight - inset).toFloat(),
                    top + 1f,
                    paint
                )
            }
        }
    }

    class GamePreviewDiffCallback : DiffUtil.ItemCallback<ListPreview>()
    {
        override fun areItemsTheSame(
            oldItem: ListPreview, newItem: ListPreview
        ): Boolean = oldItem.gameId == newItem.gameId

        override fun areContentsTheSame(
            oldItem: ListPreview, newItem: ListPreview
        ): Boolean = oldItem == newItem
    }
}
