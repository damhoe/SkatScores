package com.damhoe.skatscores.game.application.ports.in;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.domain.SkatGame;
import com.damhoe.skatscores.score.domain.SkatScore;

public interface AddScoreToGameUseCase {
 void addScoreToGame(SkatGame skatGame, SkatScore score);
 Result<SkatScore> removeLastScore(SkatGame skatGame);
}
