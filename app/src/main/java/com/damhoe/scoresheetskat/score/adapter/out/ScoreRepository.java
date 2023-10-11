package com.damhoe.scoresheetskat.score.adapter.out;

import com.damhoe.scoresheetskat.score.application.ports.out.CUDScorePort;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScoreRepository implements CUDScorePort, GetScoresPort {
   private final ScorePersistenceAdapter adapter;

   @Inject
   public ScoreRepository(ScorePersistenceAdapter adapter) {
      this.adapter = adapter;
   }

   @Override
   public SkatScore saveScore(SkatScore score) {
      long id = adapter.insertScore(score);
      score.setId(id);
      return score;
   }

   @Override
   public boolean updateScore(SkatScore score) {
      return adapter.updateScore(score);
   }

   @Override
   public SkatScore deleteScore(long id) {
      return adapter.deleteScore(id);
   }

   @Override
   public List<SkatScore> loadScores(long gameId) {
      return adapter.loadScores(gameId);
   }

   @Override
   public SkatScore getScore(long id) {
      return adapter.getScore(id);
   }
}
