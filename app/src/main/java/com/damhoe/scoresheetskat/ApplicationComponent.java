package com.damhoe.scoresheetskat;

import android.content.Context;

import com.damhoe.scoresheetskat.app.home.HomeFragment;
import com.damhoe.scoresheetskat.game.GameModule;
import com.damhoe.scoresheetskat.game.adapter.in.ui.GameFragment;
import com.damhoe.scoresheetskat.persistance.DbHelper;
import com.damhoe.scoresheetskat.player.PlayerModule;
import com.damhoe.scoresheetskat.player.adapter.in.ui.PlayerDetailsFragment;
import com.damhoe.scoresheetskat.player.adapter.in.ui.PlayersFragment;
import com.damhoe.scoresheetskat.score.ScoreModule;
import com.damhoe.scoresheetskat.score.adapter.in.ui.ScoreFragment;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, ScoreModule.class, GameModule.class, PlayerModule.class})
public interface ApplicationComponent {
 @ApplicationContext
 Context getContext();
 DbHelper getDbHelper();
 void inject(MainActivity mainActivity);
 void inject(GameFragment gameFragment);
 void inject(ScoreFragment scoreFragment);
 void inject(HomeFragment homeFragment);
 void inject(PlayersFragment playersFragment);
 void inject(PlayerDetailsFragment playerDetailsFragment);
}