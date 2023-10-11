package com.damhoe.scoresheetskat.score.adapter.in.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.application.ports.in.CUDScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.in.LoadScoreUseCase;
import com.damhoe.scoresheetskat.score.domain.ScoreRequest;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import javax.inject.Inject;

public class ScoreViewModelFactory implements ViewModelProvider.Factory {

   private final CUDScoreUseCase cudScoreUseCase;
   private final LoadScoreUseCase loadScoreUseCase;
   private ScoreRequest scoreRequest;

   @Inject
   public ScoreViewModelFactory(CUDScoreUseCase cudScoreUseCase,
                                LoadScoreUseCase loadScoreUseCase) {
      this.cudScoreUseCase = cudScoreUseCase;
      this.loadScoreUseCase = loadScoreUseCase;
   }

   public void setScoreRequest(ScoreRequest scoreRequest) {
      this.scoreRequest = scoreRequest;
   }

   @NonNull
   @Override
   @SuppressWarnings("unchecked")
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass != ScoreViewModel.class) {
         throw new IllegalArgumentException("Only ScoreViewModel allowed in factory method.");
      }

      if (scoreRequest.getScoreId() == -1L) {
         return (T) new ScoreViewModel.Builder(cudScoreUseCase)
                 .setRequest(scoreRequest)
                 .build();
      }

      Result<SkatScore> scoreResult = loadScoreUseCase.loadScore(scoreRequest.getScoreId());

      if (scoreResult.isSuccess()) {
         return (T) new ScoreViewModel.Builder(cudScoreUseCase)
                 .setRequest(scoreRequest)
                 .fromScore(scoreResult.getValue())
                 .build();
      }

      throw new RuntimeException(String.format("Failed to load the score for id: %s Message: %s",
                      scoreRequest.getScoreId(), scoreResult.getMessage()));
   }

   @NonNull
   @Override
   @SuppressWarnings("unchecked")
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass != ScoreViewModel.class) {
         throw new IllegalArgumentException("Only ScoreViewModel allowed in factory method.");
      }

      if (scoreRequest.getScoreId() == -1) {
         return (T) new ScoreViewModel.Builder(cudScoreUseCase)
                 .setRequest(scoreRequest)
                 .build();
      }

      Result<SkatScore> scoreResult = loadScoreUseCase.loadScore(scoreRequest.getScoreId());

      if (scoreResult.isSuccess()) {
         return (T) new ScoreViewModel.Builder(cudScoreUseCase)
                 .setRequest(scoreRequest)
                 .fromScore(scoreResult.getValue())
                 .build();
      }

      throw new RuntimeException(String.format("Failed to load the score for id: %s Message: %s",
              scoreRequest.getScoreId(), scoreResult.getMessage()));
   }
}
