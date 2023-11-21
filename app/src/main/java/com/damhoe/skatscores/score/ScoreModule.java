package com.damhoe.skatscores.score;

import com.damhoe.skatscores.score.adapter.out.ScoreRepository;
import com.damhoe.skatscores.score.application.ScoreService;
import com.damhoe.skatscores.score.application.ports.in.CreateScoreUseCase;
import com.damhoe.skatscores.score.application.ports.in.GetScoreUseCase;
import com.damhoe.skatscores.score.application.ports.out.CreateScorePort;
import com.damhoe.skatscores.score.application.ports.out.GetScoresPort;

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
