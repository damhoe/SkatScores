package com.damhoe.skatscores

import android.content.Context
import com.damhoe.skatscores.app.settings.AppSettingsDialogFragment
import com.damhoe.skatscores.game.game_home.GameActivity
import com.damhoe.skatscores.game.game_home.GameModule
import com.damhoe.skatscores.game.game_home.adapter.`in`.ui.GameFragment
import com.damhoe.skatscores.game.game_statistics.presentation.GameGraphFragment
import com.damhoe.skatscores.library.LibraryFragment
import com.damhoe.skatscores.persistence.DbHelper
import com.damhoe.skatscores.player.PlayerModule
import com.damhoe.skatscores.player.adapter.`in`.ui.PlayerDetailsFragment
import com.damhoe.skatscores.player.adapter.`in`.ui.PlayersFragment
import com.damhoe.skatscores.game.score.ScoreModule
import com.damhoe.skatscores.game.score.adapter.`in`.ui.ScoreFragment
import com.damhoe.skatscores.player.adapter.`in`.ui.PlayerStatisticsFragment
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
    fun inject(fragment: GameGraphFragment)
    fun inject(fragment: ScoreFragment)
    fun inject(fragment: PlayersFragment)
    fun inject(fragment: PlayerDetailsFragment)
    fun inject(fragment: PlayerStatisticsFragment)
    fun inject(fragment: LibraryFragment)
    fun inject(fragment: AppSettingsDialogFragment)
}