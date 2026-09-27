package com.damhoe.skatscores.game.common

/** Seat dealing after [roundsPlayed] rounds; the last seat deals the first round. */
fun dealerSeat(roundsPlayed: Int, seatCount: Int): Int = Math.floorMod(roundsPlayed - 1, seatCount)
