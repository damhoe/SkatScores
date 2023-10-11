package com.damhoe.scoresheetskat.game.application.ports.in;

import androidx.annotation.Nullable;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.domain.GamePreview;
import com.damhoe.scoresheetskat.game.domain.SkatGame;

import java.util.Date;
import java.util.List;

public interface LoadGameUseCase {
    Result<SkatGame> getGame(long id);
    Result<List<GamePreview>> getGamesSince(@Nullable Date oldest); // If null return all games
    Result<List<GamePreview>> getUnfinishedGames();
}
