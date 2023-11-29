package com.damhoe.skatscores.game_statistics

import android.content.res.Resources
import android.util.TypedValue

object GraphicUtils {
    fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
}