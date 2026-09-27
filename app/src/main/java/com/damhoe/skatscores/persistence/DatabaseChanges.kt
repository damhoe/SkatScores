package com.damhoe.skatscores.persistence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** What a write touched, and so which screens have to read again. */
enum class DataTopic
{
    /** The profiles themselves: names, who exists. */
    PLAYERS,

    /** Skat lists, their seats and their rounds. */
    SKAT,

    /** Doppelkopf lists, their seats and their rounds. */
    DOPPELKOPF,
}

/**
 * The signal for "something in the database changed".
 *
 * The repositories answer with what a query returned at the moment it was asked, so nothing
 * would tell a screen already built that a list, a round or a player has been written since.
 * Every write bumps the topics it touched here, and the queries a screen is built from are
 * collected from [changes] of the topics they read.
 *
 * It used to be one counter for the whole database, so a player renamed reloaded every Skat
 * and Doppelkopf list on the home screen, and a Skat round reloaded the Doppelkopf ones.
 */
@Singleton
class DatabaseChanges @Inject constructor()
{
    private val revisions: Map<DataTopic, MutableStateFlow<Long>> =
        DataTopic.entries.associateWith { MutableStateFlow(0L) }

    fun notifyChanged(vararg topics: DataTopic) =
        topics.forEach { topic -> revisions.getValue(topic).update { it + 1 } }

    /**
     * Emits straight away, so a new collector reads once, and again after every write to any
     * of [topics]. Conflated: a collector that is busy reading sees one emission for however
     * many writes happened meanwhile.
     */
    fun changes(vararg topics: DataTopic): Flow<Unit>
    {
        require(topics.isNotEmpty()) { "Name at least one topic" }
        return combine(topics.map { revisions.getValue(it) }) { }
    }
}
