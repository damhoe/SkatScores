package com.damhoe.skatscores.shared

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.damhoe.skatscores.R
import com.google.android.material.button.MaterialButton

private const val CHIP_HEIGHT_DP = 44
private const val CHIP_GAP_DP = 8

/**
 * Fills a vertical container with rows of selection chips, one chip per value.
 *
 * Chips are inflated from [R.layout.view_sheet_chip] rather than constructed so they keep the
 * sheet chip style, and building them from data means a row can only ever offer values the
 * caller actually supports.
 *
 * @return the chip for each value, so the caller can drive checked and enabled state
 */
fun <T> LinearLayout.fillWithChipRows(
    values: List<T>,
    perRow: Int = 3,
    label: (T) -> String,
    onClick: (T) -> Unit,
): Map<T, MaterialButton>
{
    removeAllViews()

    val inflater = LayoutInflater.from(context)
    val chips = linkedMapOf<T, MaterialButton>()

    values.chunked(perRow).forEachIndexed { rowIndex, rowValues ->
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { if (rowIndex > 0) topMargin = dp(CHIP_GAP_DP) }

        rowValues.forEachIndexed { columnIndex, value ->
            val chip = inflater.inflate(R.layout.view_sheet_chip, row, false) as MaterialButton
            chip.text = label(value)
            chip.layoutParams = LinearLayout.LayoutParams(0, dp(CHIP_HEIGHT_DP), 1f).apply {
                if (columnIndex > 0) marginStart = dp(CHIP_GAP_DP)
            }
            chip.setOnClickListener { onClick(value) }

            chips[value] = chip
            row.addView(chip)
        }

        // Keep a short last row aligned with the full ones above it.
        repeat(perRow - rowValues.size) {
            val filler = View(context)
            filler.layoutParams = LinearLayout.LayoutParams(0, dp(CHIP_HEIGHT_DP), 1f).apply {
                marginStart = dp(CHIP_GAP_DP)
            }
            row.addView(filler)
        }

        addView(row)
    }

    return chips
}

private fun View.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
