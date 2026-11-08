package com.damhoe.skatscores.shared

import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/** Free text with registered players as suggestions; clearing the field reopens them. */
fun MaterialAutoCompleteTextView.setupAsPlayerField()
{
    setOnClickListener { showDropDown() }
    setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDropDown() }

    doAfterTextChanged { text ->
        if (text.isNullOrEmpty() && hasFocus()) showDropDown()
    }
}

fun MaterialAutoCompleteTextView.setPlayerSuggestions(names: List<String>)
{
    setAdapter(
        ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, names)
    )
}

/** Without opening the suggestion list. */
fun MaterialAutoCompleteTextView.setSeatName(name: String) = setText(name, false)
