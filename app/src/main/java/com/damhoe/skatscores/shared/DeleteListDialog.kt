package com.damhoe.skatscores.shared

import android.content.Context
import com.damhoe.skatscores.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.confirmListDeletion(
    listName: String,
    roundsPlayed: Int,
    onConfirm: () -> Unit,
)
{
    val message =
        if (roundsPlayed <= 0) getString(R.string.message_delete_list_empty, listName)
        else resources.getQuantityString(
            R.plurals.message_delete_list,
            roundsPlayed,
            listName,
            roundsPlayed,
        )

    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.title_delete_list)
        .setMessage(message)
        .setNegativeButton(R.string.dialog_title_button_cancel, null)
        .setPositiveButton(R.string.title_button_delete) { _, _ -> onConfirm() }
        .show()
}
