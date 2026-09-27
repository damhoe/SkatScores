package com.damhoe.skatscores.shared

import android.util.Log
import androidx.annotation.StringRes

/**
 * What the user is told when something fails: one of our own translated sentences, never the
 * exception's message. Those used to reach the screen as they were - SQLite constraint errors
 * and `require` texts, in English, on a German phone. The exception is logged instead.
 */
fun userError(@StringRes message: Int, cause: Throwable): Event<Int>
{
    Log.e("SkatScores", "Failed, telling the user so", cause)
    return Event(message)
}
