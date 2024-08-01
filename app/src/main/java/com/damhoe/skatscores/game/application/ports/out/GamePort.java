package com.damhoe.skatscores.game.application.ports.out;

import androidx.lifecycle.LiveData;

import com.damhoe.skatscores.game.domain.skat.SkatGame;
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;

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
