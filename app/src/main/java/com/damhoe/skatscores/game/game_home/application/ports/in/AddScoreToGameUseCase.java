package com.damhoe.skatscores.game.game_home.application.ports.in;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.game_home.domain.SkatGame;
import com.damhoe.skatscores.game.score.domain.SkatScore;

public interface AddScoreToGameUseCase {
 void addScoreToGame(SkatGame skatGame, SkatScore score);
 Result<SkatScore> removeLastScore(SkatGame skatGame);
}
