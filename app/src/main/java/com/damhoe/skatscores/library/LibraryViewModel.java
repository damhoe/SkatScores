package com.damhoe.skatscores.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.domain.skat.SkatGameLegacy;
import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class LibraryViewModel extends ViewModel {

    private final LoadSkatGameUseCase mLoadSkatGameUseCase;
    private final CrudSkatGameUseCase mCreateGameUseCase;

    Date oldestDate;
    Date firstOfMonth;

    @Inject
    public LibraryViewModel(
            LoadSkatGameUseCase loadSkatGameUseCase,
            CrudSkatGameUseCase createGameUseCase
    ) {
        mLoadSkatGameUseCase = loadSkatGameUseCase;
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
        return Transformations.map(mLoadSkatGameUseCase.getGamesSince(oldestDate),
                allGames -> allGames.stream()
                        .filter(preview -> preview.getDate().before(firstOfMonth))
                        .collect(Collectors.toList())
        );
    }

    LiveData<List<SkatGamePreview>> getGames() {
        return mLoadSkatGameUseCase.getGamesSince(oldestDate);
    }

    LiveData<List<SkatGamePreview>> getUnfinishedGames() {
        return mLoadSkatGameUseCase.unfinishedGames;
    }

    public Result<SkatGameLegacy> deleteGame(long id) {
        return mCreateGameUseCase.deleteSkatGame(id);
    }
}