package com.damhoe.skatscores.game.skat.adapter.presentation

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.skat.adapter.presentation.SkatScoreAdapter.SkatScoreViewHolder
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import java.util.UUID

class SkatScoreAdapter(
    private val mListener: IScoreActionListener,
) : ListAdapter<SkatScore, SkatScoreViewHolder>(SkatScoreDiffCallback())
{
    private var participantsPositions: Map<UUID, Int> = emptyMap()

    @SuppressLint("NotifyDataSetChanged")
    fun setParticipants(participants: SkatParticipants)
    {
        participantsPositions = mapOf(
            participants.foreHand.id to 0,
            participants.middleHand.id to 1,
            participants.rearHand.id to 2
        )
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SkatScoreViewHolder
    {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_score,
                parent,
                false
            )
        return SkatScoreViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: SkatScoreViewHolder,
        position: Int
    )
    {
        val score = getItem(position)

        holder.roundsText.text = (position + 1).toString()

        // Null position means a passed round, or a declarer that is not at this table any more.
        val declarerPosition = score.declarerId?.let { participantsPositions[it] }
        if (declarerPosition == null)
        {
            noPoints(holder)
        } else
        {
            val pointsArray = intArrayOf(0, 0, 0)
            pointsArray[declarerPosition] = score.toPoints()

            holder.points1Text.text = makeScoreString(pointsArray[0])
            holder.points2Text.text = makeScoreString(pointsArray[1])
            holder.points3Text.text = makeScoreString(pointsArray[2])
        }

        holder.itemView.setOnClickListener { view: View? ->
            Log.d(
                "Score Event",
                "Item clicked at position $position"
            )
        }
    }

    private fun noPoints(holder: SkatScoreViewHolder)
    {
        holder.points1Text.text = "-"
        holder.points2Text.text = "-"
        holder.points3Text.text = "-"
    }

    private fun makeScoreString(points: Int): String
    {
        if (points == 0)
        {
            return "-"
        }
        return points.toString()
    }

    override fun onBindViewHolder(
        holder: SkatScoreViewHolder,
        position: Int,
        payloads: MutableList<Any>
    )
    {
        if (payloads.isEmpty())
        {
            // Full binding if payloads are empty
            onBindViewHolder(
                holder,
                position
            )
        } else
        {
            // Handle payloads (e.g., update item index)
            for (payload in payloads)
            {
                if (payload == "payload")
                {
                    // Update the item index view
                    holder.updateRounds(position)
                }
            }
        }
    }

    class SkatScoreViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)
    {
        var roundsText: TextView = itemView.findViewById(R.id.round_text)
        var points1Text: TextView = itemView.findViewById(R.id.points1_text)
        var points2Text: TextView = itemView.findViewById(R.id.points2_text)
        var points3Text: TextView = itemView.findViewById(R.id.points3_text)

        fun updateRounds(position: Int)
        {
            roundsText.text = position.toString()
        }
    }

    class ItemDecoration : RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        )
        {
            outRect.top = 1
        }
    }

    class SkatScoreDiffCallback : DiffUtil.ItemCallback<SkatScore>()
    {
        override fun areItemsTheSame(
            oldItem: SkatScore,
            newItem: SkatScore
        ): Boolean
        {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SkatScore,
            newItem: SkatScore
        ): Boolean
        {
            return oldItem == newItem
        }
    }
}
