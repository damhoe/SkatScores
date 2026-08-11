package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

interface IScoreActionListener
{
    /**
     * Opens the round sheet on an existing round. A tap is the only thing a row does:
     * removing a round is the undo action's job, and it only ever removes the newest one.
     */
    fun notifyEdit(skatScore: SkatScore)
}
