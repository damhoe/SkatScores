package com.damhoe.skatscores.game.application

import com.damhoe.skatscores.game.application.PlayerSelectionValidationResult.DuplicateName
import com.damhoe.skatscores.game.application.PlayerSelectionValidationResult.EmptyName
import com.damhoe.skatscores.game.application.PlayerSelectionValidationResult.NewPlayer
import com.damhoe.skatscores.game.application.PlayerSelectionValidationResult.Success
import com.damhoe.skatscores.player.domain.Player

class PlayerSelectionValidator(
    var allPlayers: MutableList<Player>,
    var selectedNames: MutableList<String>
)
{
    fun validate(): List<PlayerSelectionValidationResult>
    {
        val results = mutableListOf<PlayerSelectionValidationResult>()

        for (name in selectedNames)
        {
            if (name.isEmpty())
            {
                results.add(
                    EmptyName
                )
                continue
            }

            val occurrenceCount = selectedNames.stream().filter { s: String -> s == name }.count()
            if (occurrenceCount >= 2)
            {
                results.add(
                    DuplicateName
                )
                continue
            }

            if (isDummyName(name))
            {
                results.add(
                    Success
                )
            }

            if (allPlayers.stream().noneMatch { p: Player -> p.name == name })
            {
                results.add(
                    NewPlayer
                )
                continue
            }

            results.add(
                Success
            )
        }

        return results
    }

    fun isDummyName(
        name: String
    ): Boolean
    {
        return name.matches(
            Player.DUMMY_PATTERN.toRegex())
    }

    fun select(
        index: Int,
        name: String
    )
    {
        this.selectedNames[index] = name
    }
}
