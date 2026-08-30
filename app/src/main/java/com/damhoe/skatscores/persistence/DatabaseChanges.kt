package com.damhoe.skatscores.persistence

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One signal for "something in the database changed".
 *
 * The repositories answer with what a query returned at the moment it was asked, so nothing
 * would tell a screen already built that a list, a round or a player has been written since.
 * Every write bumps the revision here and the queries a screen is built from are collected
 * from it, which is what makes a new list show up on the home screen without restarting the
 * app.
 *
 * One counter for the whole database is deliberate: the tables are small, writes are rare -
 * one per round played - and a per-table signal would have to know which screen cares about
 * which join.
 */
@Singleton
class DatabaseChanges @Inject constructor()
{
    private val _revision = MutableStateFlow(0L)

    /** Holds the current revision, so a new collector reads once straight away. */
    val revision: StateFlow<Long> = _revision.asStateFlow()

    fun notifyChanged() = _revision.update { it + 1 }
}
