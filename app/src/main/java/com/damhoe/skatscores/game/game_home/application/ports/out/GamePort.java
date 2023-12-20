package com.damhoe.skatscores.game.game_home.application.ports.out;

import androidx.lifecycle.LiveData;

import com.damhoe.skatscores.game.game_home.domain.SkatGame;
import com.damhoe.skatscores.game.game_home.domain.SkatGamePreview;

import java.util.Date;
import java.util.List;

public interface GamePort {
 SkatGame saveSkatGame(SkatGame game);
 SkatGame deleteGame(long id);
 SkatGame updateGame(SkatGame game);
 SkatGame getGame(long id);
 LiveData<List<SkatGamePreview>> getUnfinishedGames();
 LiveData<List<SkatGamePreview>> getGamesSince(Date since);
 void refreshGamePreviews();
}
