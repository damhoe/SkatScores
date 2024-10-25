package com.damhoe.skatscores.player.domain

import com.damhoe.skatscores.shared.DomainType
import java.time.OffsetDateTime

open class Player(
    val id: PlayerId,
    val name: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime) :
    DomainType(
        createdAt,
        updatedAt)
{
    override fun toString() = name
}
