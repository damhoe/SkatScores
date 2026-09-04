package com.damhoe.skatscores.player.adapter.presentation

import android.content.res.Resources
import com.damhoe.skatscores.R
import com.damhoe.skatscores.player.domain.ListCounts

/**
 * "2 Skat · 3 Doppelkopf" - how many lists a player appears in, per game.
 *
 * A bare total told the reader how much a player has played but not what: profiles are shared
 * between the two games, so "5 lists" could be five of either. Naming the games costs the same
 * line and answers both.
 *
 * A game the player has never sat down to is left out rather than shown at zero, the way the
 * statistics screen hides a section with no rounds behind it.
 */
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
