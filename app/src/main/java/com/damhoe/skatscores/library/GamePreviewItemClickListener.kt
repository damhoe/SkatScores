package com.damhoe.skatscores.library

import com.damhoe.skatscores.game.skat.domain.SkatGamePreview

interface GamePreviewItemClickListener
{
    fun notifyDelete(skatGamePreview: SkatGamePreview)
    fun notifySelect(skatGamePreview: SkatGamePreview)
}