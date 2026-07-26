package com.damhoe.skatscores.persistence

import android.database.Cursor
import java.util.UUID

fun Cursor.getLong(columnName: String, defaultValue: Long): Long
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex)) getLong(columnIndex) else defaultValue
}

fun Cursor.getString(columnName: String, defaultValue: String): String
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex)) getString(columnIndex) else defaultValue
}

fun Cursor.getInt(columnName: String, defaultValue: Int): Int
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex)) getInt(columnIndex) else defaultValue
}

fun Cursor.getUuid(columnName: String, defaultValue: UUID): UUID
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex))
    {
        UUID.fromString(getString(columnIndex))
    } else
    {
        defaultValue
    }
}

fun Cursor.getLongOrNull(columnName: String): Long?
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex)) getLong(columnIndex) else null
}

fun Cursor.getStringOrNull(columnName: String): String?
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex)) getString(columnIndex) else null
}

fun Cursor.getIntOrNull(columnName: String): Int?
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex)) getInt(columnIndex) else null
}

fun Cursor.getUuidOrNull(columnName: String): UUID?
{
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1 && !isNull(columnIndex))
    {
        getString(columnIndex)?.let { UUID.fromString(it) }
    } else
    {
        null
    }
}

inline fun <T> Cursor.mapToOneOrNull(transform: (Cursor) -> T): T?
{
    this.use {
        if (it.moveToFirst())
        {
            return transform(it)
        }
        return null
    }
}

inline fun <T> Cursor.mapToList(transform: (Cursor) -> T): List<T>
{
    val list = mutableListOf<T>()
    this.use {
        if (it.moveToFirst())
        {
            do
            {
                list.add(transform(it))
            } while (it.moveToNext())
        }
    }
    return list
}
