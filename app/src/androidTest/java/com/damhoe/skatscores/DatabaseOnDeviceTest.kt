package com.damhoe.skatscores

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.damhoe.skatscores.persistence.DatabaseHelper
import com.damhoe.skatscores.persistence.Schema
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** The database on the device's own SQLite; logic is covered by the Robolectric tests. */
@RunWith(AndroidJUnit4::class)
class DatabaseOnDeviceTest
{
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "on-device-test.db"
    private val helper = DatabaseHelper(context, name, Schema.VERSION)

    @After
    fun tearDown()
    {
        helper.close()
        context.deleteDatabase(name)
    }

    @Test
    fun theAppUnderTestIsSkatScores()
    {
        assertEquals("com.damhoe.skatscores", context.packageName)
    }

    @Test
    fun foreignKeysAreEnforced()
    {
        helper.readableDatabase.rawQuery("PRAGMA foreign_keys", null).use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
        }
    }

    @Test
    fun theDatabaseRunsInWriteAheadLogMode()
    {
        helper.readableDatabase.rawQuery("PRAGMA journal_mode", null).use {
            it.moveToFirst()
            assertEquals("wal", it.getString(0).lowercase())
        }
    }

    @Test
    fun aNewDatabaseIsAtTheCurrentVersion()
    {
        assertEquals(Schema.VERSION, helper.readableDatabase.version)
    }
}
