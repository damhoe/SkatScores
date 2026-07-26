package com.damhoe.skatscores.player.adapter.presentation

import java.util.UUID

interface NotifyItemClickListener
{
    fun notifyItemClick(
        playerId: UUID,
        position: Int,
    )
}
