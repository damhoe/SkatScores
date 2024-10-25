package com.damhoe.skatscores.game;

import com.damhoe.skatscores.game.score.adapter.out.ScoreRepository;
import com.damhoe.skatscores.game.domain.skat.application.ScoreService;
import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScoreUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.GetScoreUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScorePort;
import com.damhoe.skatscores.game.domain.skat.application.ports.GetScoresPort;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ScoreModule {
   @Binds
   abstract CreateScoreUseCase bindCUDScoreUseCase(ScoreService scoreService);
   @Binds
   abstract GetScoreUseCase bindLoadScoreUseCase(ScoreService scoreService);
   @Binds
   abstract CreateScorePort bindCUDScorePort(ScoreRepository scoreRepository);
   @Binds
   abstract GetScoresPort bindGetScoresPort(ScoreRepository scoreRepository);
}
