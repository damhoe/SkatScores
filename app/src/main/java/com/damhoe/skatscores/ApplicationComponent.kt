package com.damhoe.skatscores

import android.content.Context
import com.damhoe.skatscores.game.GameActivity
import com.damhoe.skatscores.game.GameModule
import com.damhoe.skatscores.game.adapter.`in`.ui.GameFragment
import com.damhoe.skatscores.library.LibraryFragment
import com.damhoe.skatscores.persistence.DbHelper
import com.damhoe.skatscores.player.PlayerModule
import com.damhoe.skatscores.player.adapter.`in`.ui.PlayerDetailsFragment
import com.damhoe.skatscores.player.adapter.`in`.ui.PlayersFragment
import com.damhoe.skatscores.score.ScoreModule
import com.damhoe.skatscores.score.adapter.`in`.ui.ScoreFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ScoreModule::class,
    GameModule::class, PlayerModule::class])
interface ApplicationComponent {
    @get:ApplicationContext
    val context: Context
    val dbHelper: DbHelper
    fun inject(activity: MainActivity)
    fun inject(activity: GameActivity)
    fun inject(fragment: GameFragment)
    fun inject(fragment: ScoreFragment)
    fun inject(fragment: LibraryFragment)
    fun inject(fragment: PlayersFragment)
    fun inject(fragment: PlayerDetailsFragment)
}