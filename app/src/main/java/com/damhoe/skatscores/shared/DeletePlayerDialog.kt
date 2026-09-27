package com.damhoe.skatscores.shared

import android.content.Context
import com.damhoe.skatscores.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
