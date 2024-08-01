package com.damhoe.skatscores.game.application.ports.in;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.domain.skat.SkatGame;
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand;

public interface CreateGameUseCase {
   Result<SkatGame> createSkatGame(SkatGameCommand command);
   Result<SkatGame> deleteSkatGame(long id);

   /**
    * Update the title, players or settings of the game
    * @param game game to which the changes have been applied
    * @return updated game
    */
   Result<SkatGame> updateSkatGame(SkatGame game);
}
