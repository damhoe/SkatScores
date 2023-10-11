package com.damhoe.scoresheetskat.score.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;

public interface LoadScoreUseCase {
 List<SkatScore> loadScores(long gameId);
 Result<SkatScore> loadScore(long id);
}
