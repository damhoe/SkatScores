package com.damhoe.skatscores.game.application;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.damhoe.skatscores.KotlinResultWrapper;
import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.domain.skat.SkatGame;
import com.damhoe.skatscores.game.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.application.ports.in.CreateGameUseCase;
import com.damhoe.skatscores.game.application.ports.in.LoadGameUseCase;
import com.damhoe.skatscores.game.application.ports.out.GamePort;
import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand;
import com.damhoe.skatscores.game.score.application.ports.in.CreateScoreUseCase;
import com.damhoe.skatscores.game.domain.score.SkatScore;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class GameService implements CreateGameUseCase, AddScoreToGameUseCase, LoadGameUseCase {
    private final GamePort crudGamePort;
    private final CreateScoreUseCase createScoreUseCase;

    @Inject
    public GameService(GamePort crudGamePort, CreateScoreUseCase createScoreUseCase) {
        this.crudGamePort = crudGamePort;
        this.createScoreUseCase = createScoreUseCase;
    }


    @Override
    public void addScoreToGame(SkatGame skatGame, SkatScore score) {
        // Set game data
        int index = skatGame.getScores().size();
        long gameId = skatGame.getId();
        score.setGameId(gameId);
        // Validation
        // Add score to game
        skatGame.addScore(score);

        crudGamePort.refreshGamePreviews();
    }

    @Override
    public Result<SkatScore> removeLastScore(SkatGame skatGame) {
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
    public Result<SkatGame> createSkatGame(SkatGameCommand command) {
        // Check for valid title
        if (isInvalidTitle(command.getTitle())) {
            return Result.failure("Please provide a non-empty title!");
        }
        // Create game from command
        SkatGame skatGame = new SkatGame.SkatGameBuilder()
                .fromCommand(command)
                .build();
        return Result.success(crudGamePort.saveSkatGame(skatGame));
    }

    @Override
    public Result<SkatGame> deleteSkatGame(long id) {
        SkatGame deletedGame = crudGamePort.deleteGame(id);
        if (deletedGame == null) {
            return Result.failure("Unable to delete SkatGame with id: " + id);
        }
        return Result.success(deletedGame);
    }

    @Override
    public Result<SkatGame> updateSkatGame(SkatGame game) {
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
    public Result<SkatGame> getGame(long id) {
        SkatGame game = crudGamePort.getGame(id);
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
        return crudGamePort.getUnfinishedGames();
    }

    @Override
    public void refreshDataFromRepository() {
        crudGamePort.refreshGamePreviews();
    }
}
