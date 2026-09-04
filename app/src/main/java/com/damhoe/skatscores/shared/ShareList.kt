package com.damhoe.skatscores.shared

import android.content.Intent
import androidx.fragment.app.Fragment
import com.damhoe.skatscores.R
import com.damhoe.skatscores.game.common.SharedList
import com.damhoe.skatscores.game.common.asShareText

/**
 * Hands a list to the system share sheet as plain text.
 *
 * Text rather than a file: a list is read in the chat it is sent to, and text needs no
 * attachment, no provider and no permission. The subject only matters to the few targets that
 * have one, such as mail, where the list name becomes the line the message is filed under.
 *
 * Returns false when nothing could take it, which the caller reports where it has an anchor
 * for a snackbar.
 */
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
