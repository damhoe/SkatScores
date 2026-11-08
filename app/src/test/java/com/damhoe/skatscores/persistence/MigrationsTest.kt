package com.damhoe.skatscores.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesPersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.SkatGamesRepositoryImpl
import com.damhoe.skatscores.game.skat.adapter.persistence.players.SkatParticipantsPersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScorePersistenceAdapter
import com.damhoe.skatscores.game.skat.adapter.persistence.scores.SkatScoreRepositoryImpl
import com.damhoe.skatscores.game.common.Participant
import com.damhoe.skatscores.game.skat.domain.scores.SkatScore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationsTest
{
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "migration-test.db"
    private var helper: DatabaseHelper? = null

    private val game = UUID.randomUUID()
    private val anna = UUID.randomUUID()
    private val seats = List(3) { UUID.randomUUID() }
    private val rounds = List(3) { UUID.randomUUID() }

    @After
    fun tearDown()
    {
        helper?.close()
        context.deleteDatabase(name)
    }

    private fun createLegacy(version: Int, schema: List<String>, fill: (SQLiteDatabase) -> Unit)
    {
        object : SQLiteOpenHelper(context, name, null, version)
        {
            override fun onCreate(db: SQLiteDatabase)
            {
                schema.forEach(db::execSQL)
                fill(db)
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }.apply { writableDatabase; close() }
    }

    private fun openCurrent() = DatabaseHelper(context, name, Schema.VERSION).also { helper = it }

    /** One Skat list: Anna linked on forehand, two guests, three rounds stored out of order. */
    private fun fillSkat(db: SQLiteDatabase)
    {
        db.execSQL("INSERT INTO players (id, name) VALUES (?, 'Anna')", arrayOf(anna.toString()))
        db.execSQL(
            "INSERT INTO skat_games (id, title, player_count, round_count, scoring_mode)" +
                    " VALUES (?, 'Stammtisch', 0, 9, 'TOURNAMENT')",
            arrayOf(game.toString())
        )
        listOf("FOREHAND" to "Anna", "MIDDLEHAND" to "Ben", "REARHAND" to "Cleo")
            .forEachIndexed { i, (position, seatName) ->
                db.execSQL(
                    "INSERT INTO skat_participants (id, game_id, player_id, table_position, name)" +
                            " VALUES (?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        seats[i].toString(),
                        game.toString(),
                        if (i == 0) anna.toString() else null,
                        position,
                        seatName,
                    )
                )
            }
        // Out of order, so the order must come from the round number.
        listOf(2, 0, 1).forEach { round ->
            db.execSQL(
                "INSERT INTO skat_scores (id, game_id, skat_participant_id, round, score_type," +
                        " won_or_lost, grand_or_suit_suit, grand_or_suit_spitzen_value)" +
                        " VALUES (?, ?, ?, ?, 'GRAND_OR_SUIT', 'WON', 'CLUBS', 1)",
                arrayOf<Any?>(rounds[round].toString(), game.toString(), seats[round].toString(), round)
            )
        }
    }

    private fun skatRepository(db: DatabaseHelper) = SkatGamesRepositoryImpl(
        db,
        SkatGamesPersistenceAdapter(db),
        SkatParticipantsPersistenceAdapter(db),
        SkatScorePersistenceAdapter(db),
        DatabaseChanges(),
    )

    @Test
    fun `a version 2 list survives the upgrade with its rounds in order`() = runTest {
        createLegacy(2, LegacySchemas.VERSION_2, ::fillSkat)

        val loaded = skatRepository(openCurrent()).get(game).getOrThrow()

        assertNotNull(loaded)
        assertEquals(rounds, loaded!!.scores.map { it.id })
        assertEquals(
            Participant.Registered(seats[0], loaded.participants.foreHand.name, anna),
            loaded.participants.foreHand,
        )
        assertEquals("Stammtisch", loaded.title.value)
    }

    @Test
    fun `a version 1 database is upgraded too, broken foreign keys and all`() = runTest {
        createLegacy(1, LegacySchemas.VERSION_1, ::fillSkat)

        val db = openCurrent()
        val loaded = skatRepository(db).get(game).getOrThrow()

        assertEquals(3, loaded!!.scores.size)
        db.readableDatabase.rawQuery("PRAGMA foreign_key_check", null).use {
            assertEquals(0, it.count)
        }
    }

    @Test
    fun `foreign keys are enforced once the database is open`()
    {
        createLegacy(2, LegacySchemas.VERSION_2, ::fillSkat)

        openCurrent().readableDatabase.rawQuery("PRAGMA foreign_keys", null).use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
        }
    }

    @Test
    fun `a round can be added after the upgrade and lands last`() = runTest {
        createLegacy(2, LegacySchemas.VERSION_2, ::fillSkat)
        val db = openCurrent()
        val scores = SkatScoreRepositoryImpl(
            db, SkatScorePersistenceAdapter(db), RoundSequence(), DatabaseChanges()
        )

        val newRound = SkatScore.Passe.create()
        scores.insert(newRound, game).getOrThrow()

        val loaded = skatRepository(db).get(game).getOrThrow()!!
        assertEquals(rounds + newRound.id, loaded.scores.map { it.id })
    }

    @Test
    fun `rows that could never be read are left behind, dangling links cleared`() = runTest {
        val orphanGame = UUID.randomUUID()
        val ghostPlayer = UUID.randomUUID()

        createLegacy(2, LegacySchemas.VERSION_2) { db ->
            fillSkat(db)
            // A seat linked to a player that was deleted.
            db.execSQL(
                "UPDATE skat_participants SET player_id = ? WHERE id = ?",
                arrayOf(ghostPlayer.toString(), seats[1].toString())
            )
            // A round of a list that is gone.
            db.execSQL(
                "INSERT INTO skat_scores (id, game_id, round, score_type) VALUES (?, ?, 0, 'PASSE')",
                arrayOf(UUID.randomUUID().toString(), orphanGame.toString())
            )
        }

        val db = openCurrent()
        val loaded = skatRepository(db).get(game).getOrThrow()!!

        assertTrue(loaded.participants.middleHand is Participant.Guest)
        db.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM skat_scores WHERE game_id = ?",
            arrayOf(orphanGame.toString())
        ).use {
            it.moveToFirst()
            assertEquals(0, it.getInt(0))
        }
    }

    @Test
    fun `an upgraded database has the same shape as a new one`()
    {
        createLegacy(1, LegacySchemas.VERSION_1, ::fillSkat)
        val upgraded = describe(openCurrent().readableDatabase)
        helper?.close()
        context.deleteDatabase(name)

        val fresh = describe(openCurrent().readableDatabase)

        assertEquals(fresh, upgraded)
    }

    /** Ignores how the CREATE statements are spelled. */
    private fun describe(db: SQLiteDatabase): Map<String, List<String>>
    {
        val tables = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type = 'table'" +
                    " AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata' ORDER BY name",
            null
        ).use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }

        return tables.associateWith { table ->
            val columns = db.rawQuery("PRAGMA table_info($table)", null).use { c ->
                buildList {
                    while (c.moveToNext())
                    {
                        add("${c.getString(1)} ${c.getString(2)} notnull=${c.getInt(3)}" +
                                " default=${c.getString(4)} pk=${c.getInt(5)}")
                    }
                }
            }
            val indexes = db.rawQuery("PRAGMA index_list($table)", null).use { c ->
                buildList { while (c.moveToNext()) add("index ${c.getString(1)}") }
            }.sorted()
            val foreignKeys = db.rawQuery("PRAGMA foreign_key_list($table)", null).use { c ->
                buildList {
                    while (c.moveToNext()) add("fk ${c.getString(2)}.${c.getString(4)}")
                }
            }.sorted()
            columns + indexes + foreignKeys
        }
    }

    @Test
    fun `a new database has its device id`()
    {
        openCurrent().readableDatabase.rawQuery(
            "SELECT value FROM sync_meta WHERE key = 'device_id'", null
        ).use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getString(0).isNotBlank())
        }
    }
}
