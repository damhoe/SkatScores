package com.damhoe.scoresheetskat.game.domain;

import com.damhoe.scoresheetskat.game.application.ports.out.GamePort;
import com.damhoe.scoresheetskat.game.application.ports.out.PlayerMatchPort;
import com.damhoe.scoresheetskat.game.application.ports.out.ScoreMatchPort;
import com.damhoe.scoresheetskat.game_setup.domain.GameCommand;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

public class SkatGameFactory extends GameFactory<SkatGame, SkatSettings, SkatScore> {

    public SkatGameFactory(GamePort gamePort,
                           ScoreMatchPort scoreMatchPort,
                           PlayerMatchPort playerMatchPort) {
        super(gamePort, scoreMatchPort, playerMatchPort);
    }

    @Override
    public <T extends GameCommand<SkatSettings>> SkatGame createGame(T command) {
        return null;
    }

    @Override
    public SkatGame loadGame(long id) {
        return null;
    }
}
