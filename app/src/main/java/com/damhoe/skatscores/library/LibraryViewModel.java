package com.damhoe.skatscores.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.game_home.application.ports.in.CreateGameUseCase;
import com.damhoe.skatscores.game.game_home.application.ports.in.LoadGameUseCase;
import com.damhoe.skatscores.game.game_home.domain.SkatGame;
import com.damhoe.skatscores.game.game_home.domain.SkatGamePreview;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class LibraryViewModel extends ViewModel {

    private final LoadGameUseCase mLoadGameUseCase;
    private final CreateGameUseCase mCreateGameUseCase;

    Date oldestDate;
    Date firstOfMonth;

    @Inject
    public LibraryViewModel(
            LoadGameUseCase loadGameUseCase,
            CreateGameUseCase createGameUseCase
    ) {
        mLoadGameUseCase = loadGameUseCase;
        mCreateGameUseCase = createGameUseCase;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        oldestDate = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        firstOfMonth = calendar.getTime();
    }

    LiveData<List<SkatGamePreview>> getOldGames() {
        return Transformations.map(mLoadGameUseCase.getGamesSince(oldestDate),
                allGames -> allGames.stream()
                        .filter(preview -> preview.getDate().before(firstOfMonth))
                        .collect(Collectors.toList())
        );
    }

    LiveData<List<SkatGamePreview>> getGames() {
        return mLoadGameUseCase.getGamesSince(oldestDate);
    }

    LiveData<List<SkatGamePreview>> getUnfinishedGames() {
        return mLoadGameUseCase.getUnfinishedGames();
    }

    public Result<SkatGame> deleteGame(long id) {
        return mCreateGameUseCase.deleteSkatGame(id);
    }
}