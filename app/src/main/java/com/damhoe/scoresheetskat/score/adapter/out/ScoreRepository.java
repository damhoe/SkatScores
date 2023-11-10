package com.damhoe.scoresheetskat.score.adapter.out;

import android.content.res.Resources;

import androidx.lifecycle.MutableLiveData;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.application.ports.out.CreateScorePort;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScoreRepository implements CreateScorePort, GetScoresPort {
   private final ScorePersistenceAdapter persistenceAdapter;

   @Inject
   public ScoreRepository(ScorePersistenceAdapter adapter) {
      this.persistenceAdapter = adapter;
   }

   @Override
   public SkatScore saveScore(SkatScore score) {
      // Convert to database model
      ScoreDto scoreDto = ScoreDto.fromScore(score);
      long id = persistenceAdapter.insertScore(scoreDto);
      score.setId(id);
      return score;
   }

   @Override
   public SkatScore updateScore(SkatScore score) {
      Result<ScoreDto> updateResult = persistenceAdapter.updateScore(ScoreDto.fromScore(score));
      if (updateResult.isFailure()) {
         throw new Resources.NotFoundException(updateResult.getMessage());
      }
      return updateResult.getValue().toScore();
   }

   @Override
   public SkatScore deleteScore(long id) {
      Result<ScoreDto> deleteResult = persistenceAdapter.deleteScore(id);
      if (deleteResult.isFailure()) {
         throw new Resources.NotFoundException(deleteResult.getMessage());
      }
      return deleteResult.getValue().toScore();
   }

   @Override
   public void deleteScoresForGame(long gameId) {
      persistenceAdapter.deleteScoresForGame(gameId);
   }

   @Override
   public List<SkatScore> getScores(long gameId) {
      return persistenceAdapter.getScoresForGame(gameId).stream()
              .map(ScoreDto::toScore)
              .collect(Collectors.toList());
   }

   @Override
   public SkatScore getScore(long id) {
      Result<ScoreDto> getResult = persistenceAdapter.getScore(id);
      if (getResult.isFailure()) {
         throw new Resources.NotFoundException(getResult.getMessage());
      }
      return getResult.getValue().toScore();
   }
}
