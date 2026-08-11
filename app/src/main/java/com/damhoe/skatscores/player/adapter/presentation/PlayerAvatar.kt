package com.damhoe.skatscores.player.adapter.presentation

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.damhoe.skatscores.R
import java.util.UUID

/**
 * Per-player colour for the initials avatar.
 *
 * Keyed on the player id rather than the name or the row position, so a player keeps the same
 * colour in the list, on their own page, and across renames and restarts.
 */
object PlayerAvatar
{
    /** The circle is the hue at low alpha; the initial on top of it is the hue in full. */
    private const val FILL_ALPHA = 0x2E

    fun bind(initial: TextView, playerId: UUID)
    {
        val color = colorFor(initial, playerId)

        initial.setTextColor(color)
        // A tint rather than a mutated drawable: the background is shared with every other row
        // in the list, and re-binding must not fork its constant state.
        initial.backgroundTintList =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, FILL_ALPHA))
    }

    private fun colorFor(initial: TextView, playerId: UUID): Int
    {
        val palette = initial.resources.obtainTypedArray(R.array.avatar_palette)

        return try
        {
            palette.getColor(playerId.hashCode().mod(palette.length()), 0)
        }
        finally
        {
            palette.recycle()
        }
    }
}
