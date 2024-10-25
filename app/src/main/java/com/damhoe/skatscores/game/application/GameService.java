package com.damhoe.skatscores.game.application;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.damhoe.skatscores.KotlinResultWrapper;
import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.domain.skat.SkatGameLegacy;
import com.damhoe.skatscores.game.domain.skat.application.ports.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.SkatGamePort;
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand;
import com.damhoe.skatscores.game.domain.skat.application.ports.CreateScoreUseCase;
import com.damhoe.skatscores.game.domain.score.SkatScore;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class GameService implements CrudSkatGameUseCase, AddScoreToGameUseCase, LoadSkatGameUseCase {
    private final SkatGamePort crudGamePort;
    private final CreateScoreUseCase createScoreUseCase;

    @Inject
    public GameService(SkatGamePort crudGamePort, CreateScoreUseCase createScoreUseCase) {
        this.crudGamePort = crudGamePort;
        this.createScoreUseCase = createScoreUseCase;
    }

    @Override
    public void addScoreToGame(
            SkatGameLegacy skatGame, SkatScore score) {
        // Set game data
        int index = skatGame.getScores().size();
        long gameId = skatGame.id;
        score.setGameId(gameId);
        // Validation
        // Add score to game
        skatGame.addScore(score);

        crudGamePort.refreshGamePreviews();
    }

    @Override
    public Result<SkatScore> removeLastScore(SkatGameLegacy skatGame) {
        int count = skatGame.getScores().size();
        if (count == 0) {
            return Result.failure("The provided Skat game currently has no scores.");
        }
        // Get score
        SkatScore lastScore = skatGame.getScores().get(count-1);

        // Delete score
        KotlinResultWrapper.Companion.deleteScore(createScoreUseCase, lastScore.getId());

        crudGamePort.refreshGamePreviews();
        return Result.success(skatGame.getScores().remove(count-1));
    }

    @Override
    public Result<SkatGameLegacy> createSkatGame(SkatGameCommand command) {
        // Check for valid title
        if (isInvalidTitle(command.getTitle())) {
            return Result.failure("Please provide a non-empty title!");
        }
        // Create game from command
        SkatGameLegacy skatGame = new SkatGameLegacy.SkatGameBuilder()
                .fromCommand(command)
                .build();
        return Result.success(crudGamePort.saveSkatGame(skatGame));
    }

    @Override
    public Result<SkatGameLegacy> deleteSkatGame(long id) {
        SkatGameLegacy deletedGame = crudGamePort.deleteGame(id);
        if (deletedGame == null) {
            return Result.failure("Unable to delete SkatGameLegacy with id: " + id);
        }
        return Result.success(deletedGame);
    }

    @Override
    public Result<SkatGameLegacy> updateSkatGame(SkatGameLegacy game) {
        try {
            return Result.success(crudGamePort.updateGame(game));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.failure(ex.getMessage());
        }
    }

    private static boolean isInvalidTitle(String title) {
        return title.equals("");
    }

    @Override
    public Result<SkatGameLegacy> getGame(long id) {
        SkatGameLegacy game = crudGamePort.getGame(id);
        if (game == null) {
            return Result.failure("Unable to load Game with id: " + id);
        }
        return Result.success(game);
    }

    @Override
    public LiveData<List<SkatGamePreview>> getGamesSince(@Nullable Date oldest) {
        return crudGamePort.getGamesSince(oldest);
    }

    @Override
    public LiveData<List<SkatGamePreview>> getUnfinishedGames() {
        return crudGamePort.unfinishedGames;
    }

    @Override
    public void refreshDataFromRepository() {
        crudGamePort.refreshGamePreviews();
    }
}
