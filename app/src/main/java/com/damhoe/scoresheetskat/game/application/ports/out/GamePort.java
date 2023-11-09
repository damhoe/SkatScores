package com.damhoe.scoresheetskat.game.application.ports.out;

import androidx.lifecycle.LiveData;

import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;

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
