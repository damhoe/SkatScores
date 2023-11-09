package com.damhoe.scoresheetskat.score;

import com.damhoe.scoresheetskat.score.adapter.out.ScoreRepository;
import com.damhoe.scoresheetskat.score.application.ScoreService;
import com.damhoe.scoresheetskat.score.application.ports.in.CreateScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.in.GetScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.out.CreateScorePort;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;

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
