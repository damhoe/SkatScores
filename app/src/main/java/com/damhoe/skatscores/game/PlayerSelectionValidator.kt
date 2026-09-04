package com.damhoe.skatscores.game

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
                    PlayerSelectionValidationResult.EmptyName
                )
                continue
            }

            val occurrenceCount = selectedNames.stream().filter { s: String -> s == name }.count()
            if (occurrenceCount >= 2)
            {
                results.add(
                    PlayerSelectionValidationResult.DuplicateName
                )
                continue
            }

            if (false)
            {
                results.add(
                    PlayerSelectionValidationResult.Success
                )
            }

            if (allPlayers.stream().noneMatch { p: Player -> p.name.value == name })
            {
                results.add(
                    PlayerSelectionValidationResult.NewPlayer
                )
                continue
            }

            results.add(
                PlayerSelectionValidationResult.Success
            )
        }

        return results
    }

    fun select(
        index: Int,
        name: String
    )
    {
        this.selectedNames[index] = name
    }
}
