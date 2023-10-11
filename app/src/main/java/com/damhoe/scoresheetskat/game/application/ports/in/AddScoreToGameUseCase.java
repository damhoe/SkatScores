package com.damhoe.scoresheetskat.game.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

public interface AddScoreToGameUseCase {
 void addScoreToGame(SkatGame skatGame, SkatScore score);
 Result<SkatScore> removeLastScore(SkatGame skatGame);
}
