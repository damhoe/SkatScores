package com.damhoe.skatscores.shared

import android.content.Intent
import androidx.fragment.app.Fragment
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.SharedList
import com.damhoe.skatscores.game.common.asShareText

/** False when no app can receive it. */
fun Fragment.shareList(list: SharedList): Boolean
{
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, list.title)
        putExtra(Intent.EXTRA_TITLE, list.title)
        putExtra(Intent.EXTRA_TEXT, list.asShareText())
    }

    return runCatching {
        startActivity(Intent.createChooser(send, getString(R.string.description_share)))
    }.isSuccess
}
