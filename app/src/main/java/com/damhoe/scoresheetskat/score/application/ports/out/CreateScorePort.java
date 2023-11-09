package com.damhoe.scoresheetskat.score.application.ports.out;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

public interface CreateScorePort {
    SkatScore saveScore(SkatScore score);
    SkatScore updateScore(SkatScore score);
    SkatScore deleteScore(long id);
}