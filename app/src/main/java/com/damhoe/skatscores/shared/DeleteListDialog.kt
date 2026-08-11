package com.damhoe.skatscores.shared

import android.content.Context
import com.damhoe.skatscores.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * The one confirmation for deleting a list, shared by the two places that can start it: a long
 * press on a row on home, and the delete button in the game settings sheet.
 *
 * Deleting a list is not undoable, so it asks first and names what goes with it. The running
 * list never appears in the recent rows - the hero card holds it - which is why the settings
 * sheet needs the same door.
 */
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
