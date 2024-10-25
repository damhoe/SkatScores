package com.damhoe.skatscores.shared

import java.time.OffsetDateTime

abstract class DomainType(
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime)
