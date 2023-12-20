package com.damhoe.skatscores.game.game_home.application.ports.in;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.game_home.domain.SkatGamePreview;
import com.damhoe.skatscores.game.game_home.domain.SkatGame;

import java.util.Date;
import java.util.List;

public interface LoadGameUseCase {
    Result<SkatGame> getGame(long id);
    LiveData<List<SkatGamePreview>> getGamesSince(@Nullable Date oldest); // If null return all games
    LiveData<List<SkatGamePreview>> getUnfinishedGames();
    void refreshDataFromRepository();
}
