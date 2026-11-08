package com.damhoe.skatscores.player.adapter.presentation

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.damhoe.skatscores.R

/** Colours by the slot [PlayerViewModel] deals out, not by id hash, so they rarely collide. */
object PlayerAvatar
{
    private const val FILL_ALPHA = 0x2E

    fun bind(initial: TextView, slot: Int)
    {
        val color = colorFor(initial, slot)

        initial.setTextColor(color)
        // A tint, not a mutated drawable: the background's constant state is shared.
        initial.backgroundTintList =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, FILL_ALPHA))
    }

    /** Wraps around the palette. */
    private fun colorFor(initial: TextView, slot: Int): Int
    {
        val palette = initial.resources.obtainTypedArray(R.array.avatar_palette)

        return try
        {
            palette.getColor(slot.mod(palette.length()), 0)
        }
        finally
        {
            palette.recycle()
        }
    }
}
