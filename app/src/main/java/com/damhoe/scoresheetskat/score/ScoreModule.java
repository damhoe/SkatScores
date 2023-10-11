package com.damhoe.scoresheetskat.score;

import com.damhoe.scoresheetskat.score.adapter.out.ScoreRepository;
import com.damhoe.scoresheetskat.score.application.ScoreService;
import com.damhoe.scoresheetskat.score.application.ports.in.CUDScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.in.LoadScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.out.CUDScorePort;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ScoreModule {
   @Binds
   abstract CUDScoreUseCase bindCUDScoreUseCase(ScoreService scoreService);
   @Binds
   abstract LoadScoreUseCase bindLoadScoreUseCase(ScoreService scoreService);
   @Binds
   abstract CUDScorePort bindCUDScorePort(ScoreRepository scoreRepository);
   @Binds
   abstract GetScoresPort bindGetScoresPort(ScoreRepository scoreRepository);
}
