package com.damhoe.skatscores.game.game_home.application.ports.in;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.game_setup.domain.SkatGameCommand;
import com.damhoe.skatscores.game.game_home.domain.SkatGame;

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
