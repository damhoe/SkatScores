package com.damhoe.scoresheetskat.score.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;

public interface CreateScoreUseCase {
 Result<SkatScore> createScore(SkatScoreCommand command);
 Result<SkatScore> updateScore(long id, SkatScoreCommand command);
 Result<SkatScore> deleteScore(long id);
}
