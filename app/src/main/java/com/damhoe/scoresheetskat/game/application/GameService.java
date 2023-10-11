package com.damhoe.scoresheetskat.game.application;

import androidx.annotation.Nullable;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.out.GamePort;
import com.damhoe.scoresheetskat.game.domain.GamePreview;
import com.damhoe.scoresheetskat.game_setup.domain.SkatGameCommand;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.score.application.ports.in.CUDScoreUseCase;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class GameService implements CreateGameUseCase, AddScoreToGameUseCase, LoadGameUseCase {
    private final GamePort crudGamePort;
    private final CUDScoreUseCase cudScoreUseCase;

    @Inject
    public GameService(GamePort crudGamePort, CUDScoreUseCase cudScoreUseCase) {
        this.crudGamePort = crudGamePort;
        this.cudScoreUseCase = cudScoreUseCase;
    }


    @Override
    public void addScoreToGame(SkatGame skatGame, SkatScore score) {
        // Set game data
        int index = skatGame.getScores().size();
        long gameId = skatGame.getId();
        score.setIndex(index);
        score.setGameId(gameId);
        // Validation
        // Add score to game
        skatGame.addScore(score);
        // Persist changes
        crudGamePort.addScore(skatGame.getId(), score);
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
        Result<SkatScore> deleteResult = cudScoreUseCase.deleteScore(lastScore.getId());
        if (deleteResult.isFailure()) {
            return Result.failure("Failed to remove score from game: " + deleteResult.getMessage());
        }
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
        SkatGame deletedGame = crudGamePort.deleteSkatGame(id);
        if (deletedGame == null) {
            return Result.failure("Unable to delete SkatGame with id: " + id);
        }
        return Result.success(deletedGame);
    }

    private static boolean isInvalidTitle(String title) {
        return title.equals("");
    }

    @Override
    public Result<SkatGame> getGame(long id) {
        SkatGame game = crudGamePort.loadSkatGame(id);
        if (game == null) {
            return Result.failure("Unable to load Game with id: " + id);
        }
        return Result.success(game);
    }

    @Override
    public Result<List<GamePreview>> getGamesSince(@Nullable Date oldest) {
        return null;
    }

    @Override
    public Result<List<GamePreview>> getUnfinishedGames() {
        return null;
    }
}
