package com.damhoe.scoresheetskat.game.domain;

import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.game.application.ports.out.GamePort;
import com.damhoe.scoresheetskat.game.application.ports.out.PlayerMatchPort;
import com.damhoe.scoresheetskat.game.application.ports.out.ScoreMatchPort;
import com.damhoe.scoresheetskat.game_setup.domain.GameCommand;
import com.damhoe.scoresheetskat.score.domain.Score;

public abstract class GameFactory<TGame extends Game<TSettings, TScore>,
        TSettings extends GameSettings, TScore extends Score> extends ViewModel {
    protected GamePort gamePort;
    protected ScoreMatchPort scoreMatchPort;
    protected PlayerMatchPort playerMatchPort;

    public GameFactory(GamePort gamePort,
                       ScoreMatchPort scoreMatchPort,
                       PlayerMatchPort playerMatchPort) {
        this.gamePort = gamePort;
        this.scoreMatchPort = scoreMatchPort;
        this.playerMatchPort = playerMatchPort;
    }

    public abstract <T extends GameCommand<TSettings>> TGame createGame(T command);
    public abstract TGame loadGame(long id);
}
