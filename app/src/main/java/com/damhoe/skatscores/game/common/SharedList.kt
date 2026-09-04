package com.damhoe.skatscores.game.common

import com.damhoe.skatscores.shared.signed

/**
 * A list as it leaves the app, whichever game it is scored with.
 *
 * Sharing hands the list to a messenger, so what goes out is plain text: the name of the
 * list, where it stands, and the rounds behind that standing. Skat and Doppelkopf lists say
 * different things about a round - a suit and a declarer against a party and a solo - so each
 * game renders its own lines and this carries what they have in common, the way [ListPreview]
 * does for the home screen.
 *
 * Everything here is already localised text, which keeps [asShareText] free of resources and
 * testable on its own.
 */
data class SharedList(
    val title: String,
    /** Date and progress on one line, e.g. "Sa, 21. Feb · Runde 6 / 24". */
    val subtitle: String,
    /** One entry per seat, in the column order of the score board. */
    val standings: List<SharedStanding>,
    /** The played rounds, oldest first - a message reads forwards, unlike the round log. */
    val rounds: List<SharedRound>,
    val footer: String,
)

data class SharedStanding(val name: String, val total: Int)

/**
 * One round as a single line: what was played, who by and how it went, and what it came to.
 * A round with nothing to report on a part leaves it out rather than padding the line.
 */
data class SharedRound(
    val number: Int,
    val headline: String,
    val detail: String = "",
    val value: String? = null,
)

/**
 * The shared text.
 *
 * No column alignment: the receiving app picks the font, and padded columns fall apart in the
 * proportional one nearly all of them use. Blank lines carry the structure instead.
 */
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

/** "1. Marlon +142", best first, with seats on the same total sharing their place. */
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
