package com.damhoe.scoresheetskat.score.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;

public interface GetScoreUseCase {
 List<SkatScore> getScores(long gameId);
 Result<SkatScore> getScore(long id);
}
