package com.damhoe.skatscores.shared

import android.text.format.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * When a list was played, in the shortest form the locale offers, e.g. "Sa, 21. Feb".
 *
 * Uses the platform's best pattern for the skeleton rather than a fixed pattern so the day and
 * month order follow the device locale.
 */
fun Instant.asListDate(): String
{
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, LIST_DATE_SKELETON)

    return atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(pattern, locale))
}

private const val LIST_DATE_SKELETON = "EEEMMMd"
