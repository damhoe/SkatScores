package com.damhoe.skatscores.shared

import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * Seat fields are free text with the registered players offered as suggestions, so a seat can
 * be filled by picking somebody or by typing a guest's name.
 *
 * The two halves of that are easy to lose: once a name is in the box the suggestion list is
 * filtered down to it, and the box has to be emptied before anybody else can be picked. The
 * clear icon in the layout does the emptying; this reopens the list the moment it happens, so
 * clearing and choosing read as one gesture rather than two.
 */
fun MaterialAutoCompleteTextView.setupAsPlayerField()
{
    // A tap on a field that already holds a name should still offer the others.
    setOnClickListener { showDropDown() }
    setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }

    doAfterTextChanged { text ->
        if (text.isNullOrEmpty() && hasFocus()) showDropDown()
    }
}

/** Replaces the names offered by [setupAsPlayerField]'s suggestion list. */
fun MaterialAutoCompleteTextView.setPlayerSuggestions(names: List<String>)
{
    setAdapter(
        ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, names)
    )
}

/** Writes a name into a seat field without the suggestion list popping up over it. */
fun MaterialAutoCompleteTextView.setSeatName(name: String) = setText(name, false)
