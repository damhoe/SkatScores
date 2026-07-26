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
        private const val CREATE_PLAYERS_TABLE_SQL_FILE = "tables/create_players_table.sql"
        private const val CREATE_SKAT_GAMES_TABLE_SQL_FILE = "tables/create_skat_games_table.sql"
        private const val CREATE_SKAT_PARTICIPANTS_TABLE_SQL_FILE =
            "tables/create_skat_participants_table.sql"
        private const val CREATE_SKAT_SCORES_TABLE_SQL_FILE = "tables/create_skat_scores_table.sql"
    }

    override fun onCreate(database: SQLiteDatabase)
    {
        executeSqlScript(database, CREATE_PLAYERS_TABLE_SQL_FILE, context)
        executeSqlScript(database, CREATE_SKAT_GAMES_TABLE_SQL_FILE, context)
        executeSqlScript(database, CREATE_SKAT_PARTICIPANTS_TABLE_SQL_FILE, context)
        executeSqlScript(database, CREATE_SKAT_SCORES_TABLE_SQL_FILE, context)
    }

    private fun executeSqlScript(
        db: SQLiteDatabase,
        fileName: String,
        context: Context,
    )
    {
        try
        {
            context.assets.open(fileName).bufferedReader().use { reader ->
                val statementBuilder = StringBuilder()
                reader.forEachLine { line ->
                    val trimmedLine = line.trim()
                    if (trimmedLine.startsWith("--") || trimmedLine.isEmpty())
                    {
                        // Skip comments and empty lines
                    } else
                    {
                        statementBuilder.append(trimmedLine)
                            .append("\n") // Append line and a newline
                    }
                }

                val wholeScript = statementBuilder.toString().trim()

                if (wholeScript.isNotEmpty())
                {
                    db.execSQL(wholeScript)
                    Log.d(TAG, "Executed SQL script (whole file): $fileName")
                }
            }
        } catch (e: Exception)
        {
            Log.e(TAG, "Error executing SQL script from assets/$fileName", e)
        }

    }

    override fun onUpgrade(
        database: SQLiteDatabase,
        newVersion: Int,
        oldVersion: Int
    )
    {
        database.apply {
            dropTable(DatabaseConstants.SkatParticipantsTable.TABLE_NAME)
            dropTable(DatabaseConstants.SkatScoresTable.TABLE_NAME)
            dropTable(DatabaseConstants.SkatGamesTable.TABLE_NAME)
            dropTable(DatabaseConstants.PlayersTable.TABLE_NAME)
        }

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
