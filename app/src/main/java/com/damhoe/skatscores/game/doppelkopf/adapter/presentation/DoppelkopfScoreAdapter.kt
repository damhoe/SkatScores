package com.damhoe.skatscores.game.doppelkopf.adapter.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.doppelkopf.adapter.presentation.DoppelkopfScoreAdapter.RoundViewHolder
import com.damhoe.skatscores.game.doppelkopf.adapter.presentation.scores.DoppelkopfRoundTextFactory
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParticipants
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfParty
import com.damhoe.skatscores.game.doppelkopf.domain.scores.DoppelkopfScore
import com.damhoe.skatscores.shared.signed
import com.google.android.material.color.MaterialColors

/** Tapping a round opens it for editing. */
interface DoppelkopfScoreActionListener
{
    fun notifyEdit(score: DoppelkopfScore)
}

/**
 * The round log: one row per played round, newest first. Each row says who was Re, how the
 * round went and what it was worth to the winning party.
 */
class DoppelkopfScoreAdapter(
    private val listener: DoppelkopfScoreActionListener,
    private val textFactory: DoppelkopfRoundTextFactory,
) : ListAdapter<DoppelkopfScoreAdapter.Round, RoundViewHolder>(RoundDiffCallback())
{
    /** A score together with the round number it was played in and what it came to. */
    data class Round(val number: Int, val score: DoppelkopfScore, val value: Int)

    private var participants: DoppelkopfParticipants? = null

    fun setParticipants(participants: DoppelkopfParticipants)
    {
        this.participants = participants
        notifyItemRangeChanged(0, itemCount)
    }

    /** Scores arrive in playing order; the log shows the newest round at the top. */
    fun submitScores(scores: List<DoppelkopfScore>, valueOf: (DoppelkopfScore) -> Int)
    {
        submitList(
            scores
                .mapIndexed { index, score -> Round(index + 1, score, valueOf(score)) }
                .reversed()
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundViewHolder
    {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doppelkopf_score, parent, false)
        return RoundViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RoundViewHolder, position: Int)
    {
        val round = getItem(position)
        val score = round.score
        val participants = participants

        holder.roundNumber.text = round.number.toString()
        holder.title.text = textFactory.titleOf(score)
        holder.subtitle.text = participants
            ?.let { textFactory.subtitleOf(score, it) }
            .orEmpty()

        bindPartyBadge(holder, score.winner)
        bindValue(holder, round.value)

        holder.itemView.setOnClickListener { listener.notifyEdit(score) }
    }

    /** The side that took the round; the value below it is what that side scored. */
    private fun bindPartyBadge(holder: RoundViewHolder, winner: DoppelkopfParty)
    {
        holder.partyBadge.text = textFactory.partyLabel(winner)
        holder.partyBadge.setTextColor(
            MaterialColors.getColor(
                holder.partyBadge,
                if (winner == DoppelkopfParty.RE) R.attr.colorPrimary
                else R.attr.colorTertiary
            )
        )
    }

    private fun bindValue(holder: RoundViewHolder, value: Int)
    {
        holder.value.text = signed(value)
        holder.value.setTextColor(
            MaterialColors.getColor(
                holder.value,
                if (value < 0) R.attr.colorTertiary else R.attr.colorPrimary
            )
        )
    }

    class RoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val roundNumber: TextView = itemView.findViewById(R.id.round_text)
        val partyBadge: TextView = itemView.findViewById(R.id.partyBadge)
        val title: TextView = itemView.findViewById(R.id.roundTitle)
        val subtitle: TextView = itemView.findViewById(R.id.roundSubtitle)
        val value: TextView = itemView.findViewById(R.id.roundValue)
    }

    class RoundDiffCallback : DiffUtil.ItemCallback<Round>()
    {
        override fun areItemsTheSame(oldItem: Round, newItem: Round): Boolean =
            oldItem.score.id == newItem.score.id

        // DoppelkopfScore subclasses are not data classes, so compare what the row shows.
        override fun areContentsTheSame(oldItem: Round, newItem: Round): Boolean =
            oldItem.number == newItem.number &&
                    oldItem.value == newItem.value &&
                    oldItem.score.winner == newItem.score.winner &&
                    oldItem.score.soloistId == newItem.score.soloistId &&
                    oldItem.score.value == newItem.score.value
    }
}
