package com.damhoe.skatscores

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SkatScoresApp :
    Application()
{
    override fun onCreate()
    {
        super.onCreate()
    }
}
