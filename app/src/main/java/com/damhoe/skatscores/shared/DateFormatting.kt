package com.damhoe.skatscores.shared

import android.text.format.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** E.g. "Sa, 21. Feb", in the device locale's order. */
fun Instant.asListDate(): String
{
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, LIST_DATE_SKELETON)

    return atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(pattern, locale))
}

private const val LIST_DATE_SKELETON = "EEEMMMd"
