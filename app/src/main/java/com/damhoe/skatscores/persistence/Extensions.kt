package com.damhoe.skatscores.persistence

import android.database.Cursor

const val COLUMN_NOT_FOUND_INDEX = -1;

fun Cursor.getLongOrDefault(columnName: String, defaultValue: Long) =
    getColumnIndex(columnName).let {
        if (it != COLUMN_NOT_FOUND_INDEX) getLong(it)
        else defaultValue
    }

fun Cursor.getStringOrDefault(columnName: String, defaultValue: String) =
    getColumnIndex(columnName).let {
        if (it != COLUMN_NOT_FOUND_INDEX) getString(it)
        else defaultValue
    }

fun Cursor.getIntOrDefault(columnName: String, defaultValue: Int) =
    getColumnIndex(columnName).let {
        if (it != COLUMN_NOT_FOUND_INDEX) getInt(it)
        else defaultValue
    }