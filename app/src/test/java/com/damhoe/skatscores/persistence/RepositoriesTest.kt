package com.damhoe.skatscores.persistence

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.common.Title
import com.damhoe.skatscores.game.common.WonOrLost
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesPersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesRepositoryImpl
import com.damhoe.skatscores.game.skat.adapter.persistence.players.SkatParticipantsPersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScorePersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreRepositoryImpl
import com.damhoe.skatscores.game.skat.domain.SkatBid
import com.damhoe.skatscores.game.skat.domain.SkatGame
import com.damhoe.skatscores.game.skat.domain.SkatParticipants
import com.damhoe.skatscores.game.skat.domain.SkatRoundCount
import com.damhoe.skatscores.game.skat.domain.SkatScoringMode
import com.damhoe.skatscores.game.skat.domain.SkatSettings
import com.damhoe.skatscores.game.skat.domain.Spitzen
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import com.damhoe.skatscores.game.skat.domain.scores.SkatSuit
import com.damhoe.skatscores.player.adapter.persistence.PlayerPersistenceAdapter
import com.damhoe.skatscores.player.adapter.persistence.PlayerRepositoryImpl
import com.damhoe.skatscores.player.adapter.persistence.PlayerStatisticsPersistenceAdapter
import com.damhoe.skatscores.player.domain.Player
import com.damhoe.skatscores.player.domain.PlayerName
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositoriesTest
{
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "repositories-test.db"

    private val db = DatabaseHelper(context, name, Schema.VERSION)
    private val changes = DatabaseChanges()
    private val sequence = RoundSequence()

    private val games = SkatGamesRepositoryImpl(
        db,
        SkatGamesPersistenceAdapter(db),
        SkatParticipantsPersistenceAdapter(db),
        SkatScorePersistenceAdapter(db),
        changes,
    )
    private val scores = SkatScoreRepositoryImpl(db, SkatScorePersistenceAdapter(db), sequence, changes)
    private val players = PlayerRepositoryImpl(db, PlayerPersistenceAdapter(db), changes)
    private val statistics = PlayerStatisticsPersistenceAdapter(db)

    @After
    fun tearDown()
    {
        db.close()
        context.deleteDatabase(name)
    }

    private fun guest(name: String) = Participant.Guest(PlayerName(name))

    private suspend fun newList(
        fore: Participant = guest("Anna"),
        middle: Participant = guest("Ben"),
        rear: Participant = guest("Cleo"),
    ): SkatGame
    {
        val game = SkatGame.create(
            Title("Stammtisch"),
            SkatSettings(SkatRoundCount(9), SkatScoringMode.CLASSIC),
            SkatParticipants(fore, middle, rear),
        )
        games.save(game).getOrThrow()
        return game
    }

    private fun won(declarer: Participant) =
        SkatScore.GrandOrSuit.create(declarer, WonOrLost.WON, SkatSuit.CLUBS, Spitzen(1))

    private suspend fun roundsOf(game: SkatGame) =
        games.get(game.id).getOrThrow()!!.scores.map { it.id }

    @Test
    fun `undo takes back the round saved last, then the list carries on`() = runTest {
        val game = newList()
        val first = won(game.participants.foreHand)
        val second = SkatScore.Passe.create()
        scores.insert(first, game.id).getOrThrow()
        scores.insert(second, game.id).getOrThrow()

        assertEquals(second.id, scores.deleteLast(game.id).getOrThrow())
        assertEquals(listOf(first.id), roundsOf(game))

        val third = SkatScore.Passe.create()
        scores.insert(third, game.id).getOrThrow()
        assertEquals(listOf(first.id, third.id), roundsOf(game))
    }

    @Test
    fun `undo on a list without rounds removes nothing`() = runTest {
        val game = newList()

        assertNull(scores.deleteLast(game.id).getOrThrow())
    }

    @Test
    fun `rounds stay in the order they were played when the clock goes backwards`() = runTest {
        val game = newList()
        val times = ArrayDeque(listOf(5_000L, 3_000L, 1_000L))
        sequence.clock = { times.removeFirst() }

        val played = List(3) { SkatScore.Passe.create() }
        played.forEach { scores.insert(it, game.id).getOrThrow() }

        assertEquals(played.map { it.id }, roundsOf(game))
    }

    @Test
    fun `editing a round keeps its place`() = runTest {
        val game = newList()
        val first = won(game.participants.foreHand)
        scores.insert(first, game.id).getOrThrow()
        scores.insert(SkatScore.Passe.create(), game.id).getOrThrow()

        val edited = SkatScore.Overbid(first.id, first.skatParticipant, SkatSuit.GRAND, SkatBid(48))
        scores.update(edited, game.id).getOrThrow()

        val loaded = games.get(game.id).getOrThrow()!!
        assertTrue(loaded.scores.first() is SkatScore.Overbid)
    }

    @Test
    fun `two players can swap seats`() = runTest {
        val game = newList()
        val (anna, ben, cleo) = game.participants.asList()

        games.updateSeats(
            game.id,
            SkatParticipants(guest("Ben"), guest("Anna"), guest("Cleo")),
        ).getOrThrow()

        val seats = games.get(game.id).getOrThrow()!!.participants
        assertEquals(listOf("Ben", "Anna", "Cleo"), seats.asList().map { it.name.value })
        assertEquals(listOf(anna.id, ben.id, cleo.id), seats.asList().map { it.id })
    }

    @Test
    fun `renaming a player renames the seats linked to them`() = runTest {
        val anna = Player(name = PlayerName("Anna"))
        players.insert(anna).getOrThrow()
        val game = newList(fore = Participant.Registered(UUID.randomUUID(), anna.name, anna.id))

        players.update(anna.updateName(PlayerName("Anne"))).getOrThrow()

        val seat = games.get(game.id).getOrThrow()!!.participants.foreHand
        assertEquals("Anne", seat.name.value)
        assertTrue(seat is Participant.Registered)
    }

    @Test
    fun `deleting a player keeps their seats as guests and frees the name`() = runTest {
        val anna = Player(name = PlayerName("Anna"))
        players.insert(anna).getOrThrow()
        val game = newList(fore = Participant.Registered(UUID.randomUUID(), anna.name, anna.id))

        players.delete(anna.id).getOrThrow()

        val seat = games.get(game.id).getOrThrow()!!.participants.foreHand
        assertTrue(seat is Participant.Guest)
        assertEquals("Anna", seat.name.value)
        assertNull(players.get(anna.id).getOrThrow())
        players.insert(Player(name = PlayerName("Anna"))).getOrThrow()
    }

    @Test
    fun `a deleted list is gone from the home screen and from the statistics`() = runTest {
        val anna = Player(name = PlayerName("Anna"))
        players.insert(anna).getOrThrow()
        val fore = Participant.Registered(UUID.randomUUID(), anna.name, anna.id)
        val game = newList(fore = fore)
        scores.insert(won(fore), game.id).getOrThrow()

        assertEquals(1, statistics.getPlayerStatistics(anna.id).getOrThrow().skat.soloRoundsWon)

        games.delete(game.id).getOrThrow()

        assertTrue(games.getAll().first().isEmpty())
        assertNull(games.get(game.id).getOrThrow())
        val stats = statistics.getPlayerStatistics(anna.id).getOrThrow().skat
        assertEquals(0, stats.listsPlayed)
        assertEquals(0, stats.roundsPlayed)
    }

    @Test
    fun `statistics count declared, won and defended rounds in one read`() = runTest {
        val anna = Player(name = PlayerName("Anna"))
        players.insert(anna).getOrThrow()
        val fore = Participant.Registered(UUID.randomUUID(), anna.name, anna.id)
        val game = newList(fore = fore)
        val ben = game.participants.middleHand

        scores.insert(won(fore), game.id).getOrThrow()
        scores.insert(
            SkatScore.GrandOrSuit.create(fore, WonOrLost.LOST, SkatSuit.HEARTS, Spitzen(2)),
            game.id,
        ).getOrThrow()
        scores.insert(SkatScore.Overbid.create(ben, SkatSuit.SPADES, SkatBid(30)), game.id)
            .getOrThrow()
        scores.insert(SkatScore.Passe.create(), game.id).getOrThrow()
        // Undone rounds do not count.
        scores.insert(won(fore), game.id).getOrThrow()
        scores.deleteLast(game.id).getOrThrow()

        val stats = statistics.getPlayerStatistics(anna.id).getOrThrow().skat
        assertEquals(1, stats.listsPlayed)
        assertEquals(4, stats.roundsPlayed)
        assertEquals(2, stats.soloRoundsPlayed)
        assertEquals(1, stats.soloRoundsWon)
        assertEquals(1, stats.roundsWonAsOpponent)

        assertEquals(1, statistics.getAllListCounts().getOrThrow()[anna.id]?.skat)
    }

    @Test
    fun `the list flow follows every write`() = runTest {
        val game = newList()
        val observed = games.observe(game.id)

        assertEquals(0, observed.first()!!.scores.size)
        scores.insert(SkatScore.Passe.create(), game.id).getOrThrow()
        assertEquals(1, observed.first()!!.scores.size)

        games.delete(game.id).getOrThrow()
        assertNull(observed.first())
    }
}
