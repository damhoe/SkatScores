package com.damhoe.skatscores.game.skat.adapter.presentation

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.skat.adapter.presentation.SkatScoreAdapter.SkatScoreViewHolder
import com.damhoe.skatscores.game.skat.adapter.presentation.scores.RoundTextFactory
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.google.android.material.color.MaterialColors

/**
 * The round log: one row per played round, newest first. The rows describe what was played
 * instead of spreading a single value across three mostly empty columns.
 */
class SkatScoreAdapter(
    private val listener: IScoreActionListener,
    private val textFactory: RoundTextFactory,
) : ListAdapter<SkatScoreAdapter.Round, SkatScoreViewHolder>(RoundDiffCallback())
{
    /** A score together with the round number it was played in. */
    data class Round(val number: Int, val score: SkatScore)

    private var participants: SkatParticipants? = null

    fun setParticipants(participants: SkatParticipants)
    {
        this.participants = participants
        notifyItemRangeChanged(0, itemCount)
    }

    /** Scores arrive in playing order; the log shows the newest round at the top. */
    fun submitScores(scores: List<SkatScore>)
    {
        submitList(scores.mapIndexed { index, score -> Round(index + 1, score) }.reversed())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkatScoreViewHolder
    {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return SkatScoreViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SkatScoreViewHolder, position: Int)
    {
        val round = getItem(position)
        val score = round.score
        val participants = participants

        holder.roundNumber.text = round.number.toString()
        holder.title.text = textFactory.titleOf(score)
        holder.subtitle.text = participants
            ?.let { textFactory.subtitleOf(score, it) }
            .orEmpty()

        bindSuitIcon(holder, score)

        bindValue(holder, score)

        holder.itemView.setOnClickListener { listener.notifyEdit(score) }
    }

    private fun bindSuitIcon(holder: SkatScoreViewHolder, score: SkatScore)
    {
        val suit = RoundTextFactory.suitOf(score)
        val icon = suit?.let { RoundTextFactory.suitIconOf(it) }

        holder.suitIcon.visibility = if (icon == null) View.INVISIBLE else View.VISIBLE
        if (icon == null || suit == null) return

        holder.suitIcon.setImageResource(icon)
        holder.suitIcon.imageTintList = ColorStateList.valueOf(
            MaterialColors.getColor(holder.suitIcon, RoundTextFactory.suitColorAttrOf(suit))
        )
    }

    private fun bindValue(holder: SkatScoreViewHolder, score: SkatScore)
    {
        val points = score.toPoints()

        if (score is SkatScore.Passe)
        {
            holder.value.text = "–"
            holder.value.setTextColor(
                MaterialColors.getColor(holder.value, R.attr.colorOutlineVariant)
            )
            return
        }

        holder.value.text = if (points > 0) "+$points" else points.toString()
        holder.value.setTextColor(
            MaterialColors.getColor(
                holder.value,
                if (points < 0) R.attr.colorTertiary else R.attr.colorPrimary
            )
        )
    }

    class SkatScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val roundNumber: TextView = itemView.findViewById(R.id.round_text)
        val suitIcon: ImageView = itemView.findViewById(R.id.suitIcon)
        val title: TextView = itemView.findViewById(R.id.roundTitle)
        val subtitle: TextView = itemView.findViewById(R.id.roundSubtitle)
        val value: TextView = itemView.findViewById(R.id.roundValue)
    }

    class RoundDiffCallback : DiffUtil.ItemCallback<Round>()
    {
        override fun areItemsTheSame(oldItem: Round, newItem: Round): Boolean =
            oldItem.score.id == newItem.score.id

        // SkatScore subclasses are not data classes, so compare what the row actually shows.
        override fun areContentsTheSame(oldItem: Round, newItem: Round): Boolean =
            oldItem.number == newItem.number &&
                    oldItem.score.declarerId == newItem.score.declarerId &&
                    oldItem.score.result == newItem.score.result &&
                    oldItem.score.toPoints() == newItem.score.toPoints()
    }
}
