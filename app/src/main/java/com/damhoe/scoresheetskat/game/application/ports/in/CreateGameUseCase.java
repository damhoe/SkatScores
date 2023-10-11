package com.damhoe.scoresheetskat.game.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game_setup.domain.SkatGameCommand;
import com.damhoe.scoresheetskat.game.domain.SkatGame;

public interface CreateGameUseCase {
   Result<SkatGame> createSkatGame(SkatGameCommand command);
   Result<SkatGame> deleteSkatGame(long id);
}
