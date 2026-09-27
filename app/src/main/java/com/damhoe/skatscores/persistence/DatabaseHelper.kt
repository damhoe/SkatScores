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
    @param:ApplicationContext val context: Context,
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
    }

    init
    {
        // Readers get connections of their own and no longer queue behind a write - the home
        // screen reloading while a round is saved, say.
        setWriteAheadLoggingEnabled(true)
    }

    /*
     * onCreate and onUpgrade run in one transaction that SQLiteOpenHelper only commits - and
     * only then records the new version - if they return normally. Nothing in them catches:
     * a statement that fails rolls the whole thing back, and the next open tries again.
     * Swallowing it, as this used to, left a database at the new version with a table missing
     * for good.
     */

    override fun onCreate(database: SQLiteDatabase)
    {
        Schema.create(database)
    }

    override fun onUpgrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    )
    {
        Log.i(TAG, "Upgrading database from $oldVersion to $newVersion")
        Migrations.migrate(database, oldVersion, newVersion)
    }

    /**
     * A database newer than this build - a backup restored onto an older install, a rolled-back
     * update - is refused rather than recreated. SQLiteOpenHelper would throw here anyway; the
     * point is to never "fix" it by dropping tables, which would take the user's lists with it.
     */
    override fun onDowngrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    )
    {
        throw IllegalStateException(
            "Database is at version $oldVersion, newer than this app's $newVersion." +
                    " Refusing to downgrade, so nothing is lost."
        )
    }

    /**
     * Foreign keys are switched on here, after create or upgrade, rather than in onConfigure:
     * a migration rebuilds tables, and with enforcement on, dropping the old table would
     * cascade its delete into the new one. This goes through the helper's own setter so every
     * pooled connection gets it, not just the one that happened to run a PRAGMA.
     */
    override fun onOpen(db: SQLiteDatabase)
    {
        super.onOpen(db)
        if (!db.isReadOnly) db.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Runs [operation] as one unit of work, rolling back if any part of it fails.
     *
     * The body has to throw for that to happen - androidx's transaction marks the work
     * successful unless it does - and every adapter in this app reports failure as a
     * `Result` instead. So the body is expected to `getOrThrow()` each step, and the
     * exception that comes out is turned back into a `Result` here.
     */
    fun <T> transaction(operation: (db: SQLiteDatabase) -> T): Result<T>
    {
        return try
        {
            Result.success(writableDatabase.transaction { operation(this) })
        } catch (e: Exception)
        {
            Log.e(TAG, "Error in transaction, rolled back", e)
            Result.failure(e)
        }
    }

    /**
     * Runs several reads against one snapshot. A list read as three queries - the list, its
     * seats, its rounds - could otherwise see a round saved in between the second and the
     * third, or a list deleted halfway through.
     */
    fun <T> read(operation: (db: SQLiteDatabase) -> T): Result<T>
    {
        return try
        {
            Result.success(writableDatabase.transaction(exclusive = false) { operation(this) })
        } catch (e: Exception)
        {
            Log.e(TAG, "Error reading", e)
            Result.failure(e)
        }
    }
}
