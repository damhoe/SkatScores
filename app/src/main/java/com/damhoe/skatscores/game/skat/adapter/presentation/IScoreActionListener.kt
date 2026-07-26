package com.damhoe.skatscores.game.skat.adapter.presentation

import com.damhoe.skatscores.game.skat.domain.scores.SkatScore

interface IScoreActionListener
{
    fun notifyDelete()
    fun notifyDetails(skatScore: SkatScore)
    fun notifyEdit(skatScore: SkatScore)
}
