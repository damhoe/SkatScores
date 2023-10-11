package com.damhoe.scoresheetskat.score.application.ports.out;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;

public interface GetScoresPort {
 List<SkatScore> loadScores(long gameId);
 SkatScore getScore(long id);
}
