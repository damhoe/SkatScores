package com.damhoe.skatscores.game.common

/**
 * Which validation problem a seat has while a list's players are being edited.
 *
 * Shared by both games: a seat needs a name, and no two seats at a table may carry the same
 * one, whichever game the list is scored with.
 */
enum class SeatError
{
    NAME_REQUIRED,
    NAME_DUPLICATE,
}
