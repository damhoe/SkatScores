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

/**
 * onCreate and onUpgrade run in one transaction that is only committed, with the new version,
 * if they return normally; nothing in them may swallow an exception.
 */
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
        setWriteAheadLoggingEnabled(true)
    }

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

    /** Refuses rather than recreating the database, which would lose every list. */
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
     * Foreign keys are enabled here rather than in onConfigure: migrations rebuild tables, and
     * dropping an old table with enforcement on would cascade into the new one.
     */
    override fun onOpen(db: SQLiteDatabase)
    {
        super.onOpen(db)
        if (!db.isReadOnly) db.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Rolls back when [operation] throws. Adapters report failure as a `Result`, so the body
     * must `getOrThrow()` each step for a failure to roll back.
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

    /** Several reads against one consistent snapshot. */
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
