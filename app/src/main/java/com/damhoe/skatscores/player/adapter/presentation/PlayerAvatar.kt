package com.damhoe.skatscores.player.adapter.presentation

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.damhoe.skatscores.R

/**
 * Per-player colour for the initials avatar.
 *
 * The colour is picked by the slot [PlayerViewModel] hands out rather than by hashing the
 * player id. Hashing gave every player a colour of their own choosing, which meant they chose
 * the same one often: six colours and five players collide better than half the time - a table
 * of four could easily show three identical circles, which is exactly what the colour is there
 * to prevent.
 *
 * Slots are dealt out in order instead, so the first six players are always six different
 * colours; past that the palette repeats, which no six-colour set can avoid.
 */
object PlayerAvatar
{
    /** The circle is the hue at low alpha; the initial on top of it is the hue in full. */
    private const val FILL_ALPHA = 0x2E

    fun bind(initial: TextView, slot: Int)
    {
        val color = colorFor(initial, slot)

        initial.setTextColor(color)
        // A tint rather than a mutated drawable: the background is shared with every other row
        // in the list, and re-binding must not fork its constant state.
        initial.backgroundTintList =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, FILL_ALPHA))
    }

    /** Any slot is valid: it wraps around the palette, so callers never have to know its size. */
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
