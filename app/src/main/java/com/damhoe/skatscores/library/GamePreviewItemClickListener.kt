package com.damhoe.skatscores.library

import com.damhoe.skatscores.game.common.ListPreview

interface GamePreviewItemClickListener
{
    fun notifyDelete(preview: ListPreview)
    fun notifySelect(preview: ListPreview)
}
