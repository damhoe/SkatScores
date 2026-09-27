package com.damhoe.skatscores.persistence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

enum class DataTopic
{
    PLAYERS,

    /** Skat lists with their seats and rounds. */
    SKAT,

    /** Doppelkopf lists with their seats and rounds. */
    DOPPELKOPF,
}

/**
 * Change signal per topic. Every write notifies the topics it touched; screens re-query when a
 * topic they read changes.
 */
@Singleton
class DatabaseChanges @Inject constructor()
{
    private val revisions: Map<DataTopic, MutableStateFlow<Long>> =
        DataTopic.entries.associateWith { MutableStateFlow(0L) }

    fun notifyChanged(vararg topics: DataTopic) =
        topics.forEach { topic -> revisions.getValue(topic).update { it + 1 } }

    /** Emits immediately and after each write to any of [topics]; conflated. */
    fun changes(vararg topics: DataTopic): Flow<Unit>
    {
        require(topics.isNotEmpty()) { "Name at least one topic" }
        return combine(topics.map { revisions.getValue(it) }) { }
    }
}
