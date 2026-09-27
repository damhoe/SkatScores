package com.damhoe.skatscores.persistence

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.util.Log
import com.damhoe.skatscores.persistence.DatabaseConstants.SyncColumns

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

/**
 * An UPDATE that also stamps the row the way every write to a syncable table must: `updated_at`
 * set to now and `version` bumped by one. ContentValues cannot express `version + 1`, hence a
 * statement of its own. Returns the number of rows changed.
 *
 * [conflict] goes between UPDATE and the table, e.g. "OR IGNORE" to skip the rows a unique
 * constraint refuses instead of failing the whole statement.
 */
fun SQLiteDatabase.updateStamped(
    table: String,
    values: ContentValues,
    where: String,
    whereArgs: Array<out Any?>,
    conflict: String = "",
): Int
{
    val keys = values.keySet().toList()
    return executeStamped(
        table = table,
        assignments = keys.map { "$it = ?" },
        args = keys.map { values.get(it) } + whereArgs,
        where = where,
        conflict = conflict,
    )
}

/**
 * Marks the rows deleted rather than removing them, so the delete is a change like any other
 * that a sync can carry; see [SyncColumns.COLUMN_DELETED_AT]. Rows already deleted are left
 * alone. Returns the number of rows newly deleted.
 */
fun SQLiteDatabase.softDelete(table: String, where: String, whereArgs: Array<out Any?>): Int =
    executeStamped(
        table = table,
        assignments = listOf("${SyncColumns.COLUMN_DELETED_AT} = ${Schema.NOW}"),
        args = whereArgs.toList(),
        where = "($where) AND ${SyncColumns.COLUMN_DELETED_AT} IS NULL",
    )

private fun SQLiteDatabase.executeStamped(
    table: String,
    assignments: List<String>,
    args: List<Any?>,
    where: String,
    conflict: String = "",
): Int
{
    val set = (assignments + listOf(
        "${SyncColumns.COLUMN_UPDATED_AT} = ${Schema.NOW}",
        "${SyncColumns.COLUMN_VERSION} = ${SyncColumns.COLUMN_VERSION} + 1",
    )).joinToString(", ")

    return compileStatement("UPDATE $conflict $table SET $set WHERE $where").use { statement ->
        statement.bindAll(args)
        statement.executeUpdateDelete()
    }
}

private fun SQLiteStatement.bindAll(args: List<Any?>) =
    args.forEachIndexed { index, arg ->
        val position = index + 1
        when (arg)
        {
            null -> bindNull(position)
            is String -> bindString(position, arg)
            is Boolean -> bindLong(position, if (arg) 1 else 0)
            is Number -> bindLong(position, arg.toLong())
            else -> bindString(position, arg.toString())
        }
    }
