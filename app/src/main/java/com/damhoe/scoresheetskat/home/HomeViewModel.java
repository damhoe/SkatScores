package com.damhoe.scoresheetskat.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;

import java.util.List;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private final LoadGameUseCase mLoadGameUseCase;
    private final CreateGameUseCase mCreateGameUseCase;

    @Inject
    public HomeViewModel(
            LoadGameUseCase loadGameUseCase,
            CreateGameUseCase createGameUseCase
    ) {
        mLoadGameUseCase = loadGameUseCase;
        mCreateGameUseCase = createGameUseCase;
    }

    LiveData<List<SkatGamePreview>> getUnfinishedGames() {
        return mLoadGameUseCase.getUnfinishedGames();
    }

    public Result<SkatGame> deleteGame(long id) {
        return mCreateGameUseCase.deleteSkatGame(id);
    }
}