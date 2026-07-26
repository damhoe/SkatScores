package com.damhoe.skatscores.persistence

import android.database.sqlite.SQLiteDatabase
import android.util.Log

inline fun <T> SQLiteDatabase.run(block: SQLiteDatabase.() -> Result<T>): Result<T>
{
    return try
    {
        this.block()
    } catch (e: Exception)
    {
        Log.e(
            DatabaseConstants.DATABASE_NAME,
            "Database operation failed due to an unexpected error",
            e
        )
        Result.failure(e)
    }
}