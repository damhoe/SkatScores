package com.damhoe.skatscores.shared

fun signed(value: Int) = if (value > 0) "+$value" else value.toString()
