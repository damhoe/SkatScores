package com.damhoe.skatscores.game.game_home;

import com.damhoe.skatscores.game.game_home.adapter.out.GameRepository;
import com.damhoe.skatscores.game.game_home.application.GameService;
import com.damhoe.skatscores.game.game_home.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.game_home.application.ports.in.CreateGameUseCase;
import com.damhoe.skatscores.game.game_home.application.ports.in.LoadGameUseCase;
import com.damhoe.skatscores.game.game_home.application.ports.out.GamePort;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class GameModule {
   @Binds
   abstract CreateGameUseCase bindCreateGameUseCase(GameService gameService);
   @Binds
   abstract LoadGameUseCase bindLoadGameUseCase(GameService gameService);
   @Binds
   abstract AddScoreToGameUseCase bindAddScoreToGameUseCase(GameService gameService);
   @Binds
   abstract GamePort bindGamePort(GameRepository gameRepository);
}
