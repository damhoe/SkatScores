package com.damhoe.skatscores.player.domain

import java.util.UUID

data class PlayerId(
    val value: UUID = UUID.randomUUID())