package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.domain.Game;
import com.damhoe.scoresheetskat.game.domain.GameSettings;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game_setup.domain.GameCommand;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.score.domain.Score;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.List;

public abstract class GameViewModel<TGame extends Game<TSettings, TScore>,
        TSettings extends GameSettings, TScore extends Score> extends ViewModel {

    protected final CreateGameUseCase mCreateGameUseCase;
    protected final LoadGameUseCase mLoadGameUseCase;
    protected final AddScoreToGameUseCase mAddScoreToGameUseCase;

    private final MutableLiveData<TGame> game = new MutableLiveData<>();
    private final LiveData<String> title = Transformations.map(game, Game::getTitle);
    private final LiveData<List<Player>> players = Transformations.map(game, Game::getPlayers);
    private final LiveData<TSettings> settings = Transformations.map(game, Game::getSettings);
    private final LiveData<List<TScore>> scores = Transformations.map(game, Game::getScores);

    protected GameViewModel(
            CreateGameUseCase createGameUseCase,
            LoadGameUseCase loadGameUseCase,
            AddScoreToGameUseCase addScoreToGameUseCase
    ) {
        mCreateGameUseCase = createGameUseCase;
        mLoadGameUseCase = loadGameUseCase;
        mAddScoreToGameUseCase = addScoreToGameUseCase;
    }


    public LiveData<TGame> getGame() {
        return game;
    }

    public LiveData<String> getTitle() {
        return title;
    }

    public LiveData<List<Player>> getPlayers() {
        return players;
    }

    public void updatePlayers(List<Player> players) {
        TGame mGame = getGame().getValue();
        if (mGame != null) {
            mGame.setPlayers(players);
            setGame(mGame);
        }

        // Persist changes
        mCreateGameUseCase.updateSkatGame((SkatGame) mGame);
    }

    public void updateTitle(String newTitle) {
        TGame mGame = game.getValue();
        if (mGame != null) {
            mGame.setTitle(newTitle);
            game.postValue(mGame);

            // Persist changes
            mCreateGameUseCase.updateSkatGame((SkatGame) mGame);
        }
    }

    public void updateSettings(TSettings newSettings) {
        TGame mGame = game.getValue();
        if (mGame != null) {
            mGame.setSettings(newSettings);
            game.postValue(mGame);

            // Persist changes
            mCreateGameUseCase.updateSkatGame((SkatGame) mGame);
        }
    }

    public void updateGame(TGame updatedGame) {
        game.postValue(updatedGame);
        mCreateGameUseCase.updateSkatGame((SkatGame) updatedGame);
    }

    public LiveData<List<TScore>> getScores() {
        return scores;
    }

    public LiveData<TSettings> getSettings() {
        return settings;
    }

    protected void setGame(TGame game) {
        this.game.postValue(game);
    }

    public abstract void initialize(long gameId);
    public abstract <TCommand extends GameCommand<TSettings>> void initialize(TCommand command);
    public abstract void notifyGameIsFinished();
    public abstract void addScore(TScore score);
    public abstract Result<TScore> removeLastScore();
    public abstract void updateScore(SkatScore score);
    public abstract boolean isInitialized();
}