package com.damhoe.scoresheetskat;

import android.app.Application;
import android.content.Context;

import com.damhoe.scoresheetskat.game.adapter.out.GameRepository;
import com.damhoe.scoresheetskat.game.application.GameService;
import com.damhoe.scoresheetskat.game.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.out.GamePort;
import com.damhoe.scoresheetskat.player.adapter.out.PlayerRepository;
import com.damhoe.scoresheetskat.player.application.PlayerService;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.LoadPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.score.adapter.out.ScoreRepository;
import com.damhoe.scoresheetskat.score.application.ScoreService;
import com.damhoe.scoresheetskat.score.application.ports.in.CUDScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.in.LoadScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.out.CUDScorePort;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

   private final Application mApplication;

   protected ApplicationModule(Application app) {
      mApplication = app;
   }

   @Provides
   @ApplicationContext
   Context provideContext() {
      return mApplication;
   }

   @Provides
   @DatabaseInfo
   String provideDatabaseName() {
      return "score-sheet-skat.db";
   }

   @Provides
   @DatabaseInfo
   Integer provideDatabaseVersion() {
      return 2;
   }
}
