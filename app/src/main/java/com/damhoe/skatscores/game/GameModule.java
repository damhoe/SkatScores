package com.damhoe.skatscores.game;

import com.damhoe.skatscores.game.adapter.repositories.GameRepository;
import com.damhoe.skatscores.game.application.GameService;
import com.damhoe.skatscores.game.domain.skat.application.ports.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.SkatGamePort;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class GameModule {
   @Binds
   abstract CrudSkatGameUseCase bindCreateGameUseCase(GameService gameService);
   @Binds
   abstract LoadSkatGameUseCase bindLoadGameUseCase(GameService gameService);
   @Binds
   abstract AddScoreToGameUseCase bindAddScoreToGameUseCase(GameService gameService);
   @Binds
   abstract SkatGamePort bindGamePort(GameRepository gameRepository);
}
