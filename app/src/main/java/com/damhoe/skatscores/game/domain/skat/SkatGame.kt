package com.damhoe.skatscores.game.domain.skat

import com.damhoe.skatscores.game.domain.IGame

class SkatGame(
    val settings: SkatSettings,
    val players: SkatPlayers) : IGame
{

}