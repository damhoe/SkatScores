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
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.library.GamePreviewAdapter.GamePreviewViewHolder
import com.damhoe.skatscores.shared.asListDate

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
        val context = holder.itemView.context

        holder.rounds.text = preview.totalRounds.toString()
        holder.title.text = preview.title.value
        holder.meta.text = context.getString(
            R.string.format_list_meta,
            preview.playedAt.asListDate(),
            preview.playerNames.joinToString(", ")
        )

        // Only a played list has a leader worth showing.
        holder.winnerDelta.text = preview.leaderTotal?.let { signed(it) }.orEmpty()

        holder.itemView.setOnClickListener { itemClickListener.notifySelect(preview) }

        // Deleting a list is a long press rather than a swipe: a swipe was too easy to trigger
        // by accident on a row that is mostly there to be opened.
        holder.itemView.setOnLongClickListener {
            itemClickListener.notifyDelete(preview)
            true
        }
    }

    private fun signed(value: Int) = if (value > 0) "+$value" else value.toString()

    class GamePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val rounds: TextView = itemView.findViewById(R.id.rounds)
        val title: TextView = itemView.findViewById(R.id.title)
        val meta: TextView = itemView.findViewById(R.id.meta)
        val winnerDelta: TextView = itemView.findViewById(R.id.winnerDelta)
    }

    /**
     * 1dp hairline between rows, matching the handoff's divider.
     *
     * [inset] is the row's own side gutter in pixels. The rows run the full width of the
     * screen so that a press or a swipe reaches the edges, but the hairline still stops
     * where the text does - a divider that ran edge to edge would read as a section break
     * rather than as a separator between two rows.
     */
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

    class GamePreviewDiffCallback : DiffUtil.ItemCallback<SkatGamePreview>()
    {
        override fun areItemsTheSame(
            oldItem: SkatGamePreview, newItem: SkatGamePreview
        ): Boolean = oldItem.gameId == newItem.gameId

        override fun areContentsTheSame(
            oldItem: SkatGamePreview, newItem: SkatGamePreview
        ): Boolean = oldItem == newItem
    }
}
