package com.damhoe.skatscores.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damhoe.skatscores.game.common.GameType
import com.damhoe.skatscores.game.common.ListPreview
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.doppelkopf.application.usecases.CreateDoppelkopfGameCommand
import com.damhoe.skatscores.game.doppelkopf.application.usecases.DoppelkopfGameUseCases
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGameDefaults
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfGamePreview
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfRoundCount
import com.damhoe.skatscores.game.doppelkopf.domain.DoppelkopfSettings
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

/** Where a tap on a list has to go: the two games have separate screens. */
data class OpenList(val gameType: GameType, val gameId: UUID)

/**
 * Home.
 *
 * Skat and Doppelkopf lists never mix, so the screen shows one game at a time and the filter
 * row at the top picks which. Everything below it - the running list, quick start, recently
 * played - follows that choice.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getGamePreviewsUseCase: GetGamePreviewsUseCase,
    private val deleteSkatGameUseCase: DeleteSkatGameUseCase,
    private val createSkatGameUseCase: CreateSkatGameUseCase,
    private val doppelkopfUseCases: DoppelkopfGameUseCases,
) : ViewModel()
{
    private val allSkatGames: LiveData<List<SkatGamePreview>> =
        getGamePreviewsUseCase(GamePreviewFilter.All)

    private val allDoppelkopfGames: LiveData<List<DoppelkopfGamePreview>> =
        doppelkopfUseCases.getPreviews()

    private val _gameType = MutableLiveData(GameType.Default)
    val gameType: LiveData<GameType> = _gameType

    private val showAllRecent = MutableLiveData(false)

    /** The lists of the selected game, newest first. */
    private val lists: LiveData<List<ListPreview>> =
        MediatorLiveData<List<ListPreview>>().apply {
            fun update()
            {
                value = when (_gameType.value ?: GameType.Default)
                {
                    GameType.SKAT -> allSkatGames.value.orEmpty().map { it.toListPreview() }
                    GameType.DOPPELKOPF ->
                        allDoppelkopfGames.value.orEmpty().map { it.toListPreview() }
                }
            }
            addSource(allSkatGames) { update() }
            addSource(allDoppelkopfGames) { update() }
            addSource(_gameType) { update() }
        }

    /** The most recently played list of the selected game that still has rounds left. */
    val activeList: LiveData<ListPreview?> =
        MediatorLiveData<ListPreview?>().apply {
            addSource(lists) { previews -> value = previews.firstOrNull { it.isRunning } }
        }

    /**
     * Everything except the list already shown in the hero card, capped until the user asks
     * for all of them.
     */
    val recentGames: LiveData<List<ListPreview>> =
        MediatorLiveData<List<ListPreview>>().apply {
            fun update()
            {
                val rest = withoutActiveList()
                value = if (showAllRecent.value == true) rest else rest.take(RECENT_LIMIT)
            }
            addSource(lists) { update() }
            addSource(showAllRecent) { update() }
        }

    /** True while [recentGames] is capped and there is more to show. */
    val canShowMoreRecent: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        fun update()
        {
            value = showAllRecent.value != true && withoutActiveList().size > RECENT_LIMIT
        }
        addSource(lists) { update() }
        addSource(showAllRecent) { update() }
    }

    /** The list a quick start would be modelled on: the most recent one with known players. */
    val quickStartTemplate: LiveData<ListPreview?> =
        MediatorLiveData<ListPreview?>().apply {
            fun update()
            {
                value = when (_gameType.value ?: GameType.Default)
                {
                    GameType.SKAT -> allSkatGames.value
                        ?.firstOrNull { it.participants != null }
                        ?.toListPreview()

                    GameType.DOPPELKOPF -> allDoppelkopfGames.value
                        ?.firstOrNull { it.participants != null }
                        ?.toListPreview()
                }
            }
            addSource(allSkatGames) { update() }
            addSource(allDoppelkopfGames) { update() }
            addSource(_gameType) { update() }
        }

    private val _navigateToGame = MutableLiveData<Event<OpenList>>()
    val navigateToGame: LiveData<Event<OpenList>> = _navigateToGame

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private fun withoutActiveList(): List<ListPreview>
    {
        val previews = lists.value ?: return emptyList()
        val activeId = previews.firstOrNull { it.isRunning }?.gameId
        return previews.filter { it.gameId != activeId }
    }

    /** Switching game resets how much of the recent list is unfolded. */
    fun selectGameType(gameType: GameType)
    {
        if (_gameType.value == gameType) return

        _gameType.value = gameType
        showAllRecent.value = false
    }

    fun showAllRecent()
    {
        showAllRecent.value = true
    }

    /**
     * Deletes for real. There is no pending state to hold a list in any more: the row asks
     * for confirmation before calling this, so there is nothing left to take back.
     */
    fun confirmDelete(preview: ListPreview) = viewModelScope.launch {
        when (preview.gameType)
        {
            GameType.SKAT -> deleteSkatGameUseCase(preview.gameId)
            GameType.DOPPELKOPF -> doppelkopfUseCases.deleteGame(preview.gameId)
        }
    }

    /**
     * Starts a new list with the same people, round count and settings as the most recent
     * one, skipping setup. Participants are recreated: their ids belong to the list they were
     * recorded in.
     */
    fun quickStart(template: ListPreview) = viewModelScope.launch {
        when (template.gameType)
        {
            GameType.SKAT -> quickStartSkat(template)
            GameType.DOPPELKOPF -> quickStartDoppelkopf(template)
        }
    }

    private suspend fun quickStartSkat(template: ListPreview)
    {
        val source = allSkatGames.value?.firstOrNull { it.gameId == template.gameId } ?: return
        val participants = source.participants ?: return
        val roundCount = runCatching { SkatRoundCount(source.totalRounds) }
            .getOrDefault(SkatGameDefaults.RoundCount)

        createSkatGameUseCase(
            CreateSkatGameCommand(
                settings = SkatSettings(
                    roundCount = roundCount,
                    scoringMode = source.scoringMode,
                ),
                title = nextTitle(source.title),
                playerCount = SkatGameDefaults.PlayerCount,
                participants = participants.asNewParticipants(),
            )
        ).fold(
            onSuccess = { game ->
                _navigateToGame.postValue(Event(OpenList(GameType.SKAT, game.id)))
            },
            onFailure = { _errorMessage.postValue(Event(it.message ?: "Could not start list")) }
        )
    }

    private suspend fun quickStartDoppelkopf(template: ListPreview)
    {
        val source =
            allDoppelkopfGames.value?.firstOrNull { it.gameId == template.gameId } ?: return
        val participants = source.participants ?: return
        val roundCount = runCatching { DoppelkopfRoundCount(source.totalRounds) }
            .getOrDefault(DoppelkopfGameDefaults.RoundCount)

        doppelkopfUseCases.createGame(
            CreateDoppelkopfGameCommand(
                settings = DoppelkopfSettings(
                    roundCount = roundCount,
                    scoringMode = source.scoringMode,
                ),
                title = nextTitle(source.title),
                participants = participants.asNewParticipants(),
            )
        ).fold(
            onSuccess = { game ->
                _navigateToGame.postValue(Event(OpenList(GameType.DOPPELKOPF, game.id)))
            },
            onFailure = { _errorMessage.postValue(Event(it.message ?: "Could not start list")) }
        )
    }

    /** Name to offer for the next list, continuing the numbering of [template]. */
    fun suggestedTitleFor(template: ListPreview) = nextQuickStartTitle(template.title.value)

    private fun nextTitle(previous: Title): Title =
        Title.create(nextQuickStartTitle(previous.value)).getOrElse { previous }

    /** "Stammtisch 010" becomes "Stammtisch 011"; anything else just gets a " 2" suffix. */
    private fun nextQuickStartTitle(previous: String): String
    {
        val match = Regex("""^(.*?)(\d+)$""").find(previous) ?: return "$previous 2"
        val (prefix, digits) = match.destructured
        val next = (digits.toIntOrNull() ?: 1) + 1

        return prefix + next.toString().padStart(digits.length, '0')
    }
}
