package com.damhoe.skatscores.shared

import android.util.Log
import androidx.annotation.StringRes

/** Logs [cause] and shows a translated [message] instead of the exception text. */
fun userError(@StringRes message: Int, cause: Throwable): Event<Int>
{
    Log.e("SkatScores", "Failed, telling the user so", cause)
    return Event(message)
}
