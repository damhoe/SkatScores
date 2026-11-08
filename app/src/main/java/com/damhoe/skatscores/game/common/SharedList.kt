package com.damhoe.skatscores.game.common

import com.damhoe.skatscores.shared.signed

/** A list as plain, already localised text for sharing; each game renders its own rounds. */
data class SharedList(
    val title: String,
    /** E.g. "Sa, 21. Feb · Runde 6 / 24". */
    val subtitle: String,
    val standings: List<SharedStanding>,
    /** Oldest first, unlike the round log. */
    val rounds: List<SharedRound>,
    val footer: String,
)

data class SharedStanding(val name: String, val total: Int)

/** Empty parts are left out of the line. */
data class SharedRound(
    val number: Int,
    val headline: String,
    val detail: String = "",
    val value: String? = null,
)

/** No column alignment: receiving apps mostly use proportional fonts. */
fun SharedList.asShareText(): String = buildList {
    add(title)
    if (subtitle.isNotBlank()) add(subtitle)

    if (standings.isNotEmpty())
    {
        add("")
        addAll(standingLines())
    }

    if (rounds.isNotEmpty())
    {
        add("")
        addAll(rounds.map { it.asLine() })
    }

    if (footer.isNotBlank())
    {
        add("")
        add(footer)
    }
}.joinToString("\n")

/** "1. Marlon +142", best first; equal totals share a place. */
private fun SharedList.standingLines(): List<String> = standings
    .sortedByDescending { it.total }
    .map { standing ->
        val place = 1 + standings.count { it.total > standing.total }
        "$place. ${standing.name} ${signed(standing.total)}"
    }

private fun SharedRound.asLine(): String
{
    val parts = listOfNotNull(
        headline.takeIf { it.isNotBlank() },
        detail.takeIf { it.isNotBlank() },
        value?.takeIf { it.isNotBlank() },
    )

    return "$number. ${parts.joinToString(SEPARATOR)}"
}

private const val SEPARATOR = " · "
