package com.damhoe.skatscores.player.adapter.presentation

import android.content.res.Resources
import com.damhoe.skatscores.R
import com.damhoe.skatscores.player.domain.ListCounts

/** "2 Skat · 3 Doppelkopf"; a game with no lists is left out. */
fun Resources.listCountLabel(counts: ListCounts): String
{
    if (counts.isEmpty) return getString(R.string.label_no_lists_yet)

    val skat = countLabel(counts.skat, R.string.label_game_type_skat)
    val doppelkopf = countLabel(counts.doppelkopf, R.string.label_game_type_doppelkopf)

    return when
    {
        skat == null -> doppelkopf.orEmpty()
        doppelkopf == null -> skat
        else -> getString(R.string.format_list_meta, skat, doppelkopf)
    }
}

private fun Resources.countLabel(count: Int, gameNameRes: Int): String? =
    if (count <= 0) null
    else getString(R.string.format_game_list_count, count, getString(gameNameRes))
