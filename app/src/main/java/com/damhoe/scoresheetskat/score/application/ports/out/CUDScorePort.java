package com.damhoe.scoresheetskat.score.application.ports.out;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

public interface CUDScorePort {
    SkatScore saveScore(SkatScore score);
    boolean updateScore(SkatScore score);
    SkatScore deleteScore(long id);
}