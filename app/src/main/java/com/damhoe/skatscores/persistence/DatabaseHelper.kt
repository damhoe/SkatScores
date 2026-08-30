package com.damhoe.skatscores.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.damhoe.skatscores.DatabaseInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseHelper @Inject constructor(
    @ApplicationContext val context: Context,
    @DatabaseInfo dbName: String,
    @DatabaseInfo version: Int,
) : SQLiteOpenHelper(
    context,
    dbName,
    null,
    version,
)
{
    companion object
    {
        private const val TAG = "SkatScoreDatabase"

        /**
         * Every table the app owns, in dependency order. Each script is written with
         * IF NOT EXISTS so that running the whole list is safe on an existing database,
         * which is what an upgrade does.
         */
        private val CREATE_TABLE_SQL_FILES = listOf(
            "tables/create_players_table.sql",
            "tables/create_skat_games_table.sql",
            "tables/create_skat_participants_table.sql",
            "tables/create_skat_scores_table.sql",
            "tables/create_doppelkopf_games_table.sql",
            "tables/create_doppelkopf_participants_table.sql",
            "tables/create_doppelkopf_scores_table.sql",
        )
    }

    override fun onCreate(database: SQLiteDatabase)
    {
        CREATE_TABLE_SQL_FILES.forEach { executeSqlScript(database, it, context) }
    }

    private fun executeSqlScript(
        db: SQLiteDatabase,
        fileName: String,
        context: Context,
    )
    {
        val statements = try
        {
            // execSQL runs a single statement, so a script has to be handed to it one
            // statement at a time or everything after the first one is silently dropped.
            context.assets.open(fileName).bufferedReader().use { splitSqlStatements(it.readText()) }
        } catch (e: Exception)
        {
            Log.e(TAG, "Could not read SQL script from assets/$fileName", e)
            return
        }

        // Per statement, so that one that cannot be applied - an index an existing database
        // already violates, say - does not take the rest of the script down with it.
        statements.forEach { statement ->
            try
            {
                db.execSQL(statement)
            } catch (e: Exception)
            {
                Log.e(TAG, "Error executing statement from assets/$fileName: $statement", e)
            }
        }
    }

    /**
     * Additive by design: every create script is IF NOT EXISTS, so an upgrade adds whatever
     * tables a newer version introduced and leaves recorded lists alone. Dropping and
     * recreating would take a user's whole history with it.
     */
    override fun onUpgrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    )
    {
        Log.d(TAG, "Upgrading database from $oldVersion to $newVersion")
        onCreate(database)
    }

    fun SQLiteDatabase.dropTable(tableName: String)
    {
        execSQL("DROP TABLE IF EXISTS $tableName")
    }

    fun <T> transaction(operation: (db: SQLiteDatabase) -> T): T
    {
        return try
        {
            writableDatabase.transaction { operation(this) }
        } catch (e: Exception)
        {
            Log.e(TAG, "Error in transaction", e)
            throw e
        }
    }
}
