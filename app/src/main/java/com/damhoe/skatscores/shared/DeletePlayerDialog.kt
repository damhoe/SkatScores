package com.damhoe.skatscores.shared

import android.content.Context
import com.damhoe.skatscores.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Confirmation for deleting a player profile, next to [confirmListDeletion].
 *
 * The button used to delete straight away and leave the screen, which put an irreversible
 * action one stray tap from a page people open just to read. The lists the player sat in stay
 * where they are - their name is kept on the seat - so the message says what is actually lost:
 * the profile and the record built on it.
 */
fun Context.confirmPlayerDeletion(
    playerName: String,
    onConfirm: () -> Unit,
)
{
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.title_delete_player)
        .setMessage(getString(R.string.message_delete_player, playerName))
        .setNegativeButton(R.string.dialog_title_button_cancel, null)
        .setPositiveButton(R.string.title_button_delete) { _, _ -> onConfirm() }
        .show()
}
