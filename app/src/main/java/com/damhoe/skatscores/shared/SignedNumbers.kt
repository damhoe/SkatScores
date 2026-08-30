package com.damhoe.skatscores.shared

/** How every signed number on the redesigned screens reads. */
fun signed(value: Int) = if (value > 0) "+$value" else value.toString()
