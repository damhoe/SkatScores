package com.damhoe.skatscores;

import android.content.Context;

import com.damhoe.skatscores.game.GameActivity;
import com.damhoe.skatscores.game.GameModule;
import com.damhoe.skatscores.game.adapter.in.ui.GameFragment;
import com.damhoe.skatscores.library.LibraryFragment;
import com.damhoe.skatscores.persistence.DbHelper;
import com.damhoe.skatscores.player.PlayerModule;
import com.damhoe.skatscores.player.adapter.in.ui.PlayerDetailsFragment;
import com.damhoe.skatscores.player.adapter.in.ui.PlayersFragment;
import com.damhoe.skatscores.score.ScoreModule;
import com.damhoe.skatscores.score.adapter.in.ui.ScoreFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, ScoreModule.class, GameModule.class, PlayerModule.class})
public interface ApplicationComponent {
 @ApplicationContext
 Context getContext();
 DbHelper getDbHelper();
 void inject(MainActivity mainActivity);
 void inject(GameActivity gameActivity);
 void inject(GameFragment gameFragment);
 void inject(ScoreFragment scoreFragment);
 void inject(LibraryFragment libraryFragment);
 void inject(PlayersFragment playersFragment);
 void inject(PlayerDetailsFragment playerDetailsFragment);
}
