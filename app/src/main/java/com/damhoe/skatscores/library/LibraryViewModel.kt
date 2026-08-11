package com.damhoe.skatscores.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.skat.application.usecases.CreateSkatGameCommand
import com.damhoe.skatscores.game.skat.application.usecases.CreateSkatGameUseCase
import com.damhoe.skatscores.game.skat.application.usecases.DeleteSkatGameUseCase
import com.damhoe.skatscores.game.skat.application.usecases.GetGamePreviewsUseCase
import com.damhoe.skatscores.game.skat.domain.GamePreviewFilter
import com.damhoe.skatscores.game.skat.domain.SkatGameDefaults
import com.damhoe.skatscores.game.skat.domain.SkatGamePreview
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val RECENT_LIMIT = 5

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getGamePreviewsUseCase: GetGamePreviewsUseCase,
    private val deleteSkatGameUseCase: DeleteSkatGameUseCase,
    private val createSkatGameUseCase: CreateSkatGameUseCase,
) : ViewModel()
{
    private val allGames: LiveData<List<SkatGamePreview>> =
        getGamePreviewsUseCase(GamePreviewFilter.All)

    private val showAllRecent = MutableLiveData(false)

    /** The most recently played list that still has rounds left. */
    val activeList: LiveData<SkatGamePreview?> =
        MediatorLiveData<SkatGamePreview?>().apply {
            addSource(allGames) { previews -> value = previews.firstOrNull { it.isRunning } }
        }

    /**
     * Everything except the list already shown in the hero card, capped until the user asks
     * for all of them.
     */
    val recentGames: LiveData<List<SkatGamePreview>> =
        MediatorLiveData<List<SkatGamePreview>>().apply {
            fun update()
            {
                val rest = withoutActiveList()
                value = if (showAllRecent.value == true) rest else rest.take(RECENT_LIMIT)
            }
            addSource(allGames) { update() }
            addSource(showAllRecent) { update() }
        }

    /** True while [recentGames] is capped and there is more to show. */
    val canShowMoreRecent: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        fun update()
        {
            value = showAllRecent.value != true && withoutActiveList().size > RECENT_LIMIT
        }
        addSource(allGames) { update() }
        addSource(showAllRecent) { update() }
    }

    /** The list a quick start would be modelled on: the most recent one with known players. */
    val quickStartTemplate: LiveData<SkatGamePreview?> =
        MediatorLiveData<SkatGamePreview?>().apply {
            addSource(allGames) { previews ->
                value = previews.firstOrNull { it.participants != null }
            }
        }

    private val _navigateToGame = MutableLiveData<Event<UUID>>()
    val navigateToGame: LiveData<Event<UUID>> = _navigateToGame

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private fun withoutActiveList(): List<SkatGamePreview>
    {
        val previews = allGames.value ?: return emptyList()
        val activeId = previews.firstOrNull { it.isRunning }?.gameId
        return previews.filter { it.gameId != activeId }
    }

    fun showAllRecent()
    {
        showAllRecent.value = true
    }

    /**
     * Deletes for real. There is no pending state to hold a list in any more: the row asks
     * for confirmation before calling this, so there is nothing left to take back.
     */
    fun confirmDelete(id: UUID) = viewModelScope.launch {
        deleteSkatGameUseCase(id)
    }

    /**
     * Starts a new list with the same people, round count and scoring mode as [template],
     * skipping setup. Participants are recreated: their ids belong to the list they were
     * recorded in.
     */
    fun quickStart(template: SkatGamePreview) = viewModelScope.launch {
        val participants = template.participants ?: return@launch
        val roundCount = runCatching { SkatRoundCount(template.totalRounds) }
            .getOrDefault(SkatGameDefaults.RoundCount)
        val title = Title.create(nextQuickStartTitle(template.title.value))
            .getOrElse { template.title }

        createSkatGameUseCase(
            CreateSkatGameCommand(
                settings = SkatSettings(
                    roundCount = roundCount,
                    scoringMode = template.scoringMode,
                ),
                title = title,
                playerCount = SkatGameDefaults.PlayerCount,
                participants = participants.asNewParticipants(),
            )
        ).fold(
            onSuccess = { game -> _navigateToGame.postValue(Event(game.id)) },
            onFailure = { _errorMessage.postValue(Event(it.message ?: "Could not start list")) }
        )
    }

    /** Name to offer for the next list, continuing the numbering of [template]. */
    fun suggestedTitleFor(template: SkatGamePreview) = nextQuickStartTitle(template.title.value)

    /** "Stammtisch 010" becomes "Stammtisch 011"; anything else just gets a " 2" suffix. */
    private fun nextQuickStartTitle(previous: String): String
    {
        val match = Regex("""^(.*?)(\d+)$""").find(previous) ?: return "$previous 2"
        val (prefix, digits) = match.destructured
        val next = (digits.toIntOrNull() ?: 1) + 1

        return prefix + next.toString().padStart(digits.length, '0')
    }
}
