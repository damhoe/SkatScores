package com.damhoe.scoresheetskat.game.domain;

import com.damhoe.scoresheetskat.game_setup.domain.GameCommand;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.score.domain.Score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class Game<TSettings extends GameSettings, TScore extends Score> {
    public static final int STATE_INITIALIZED = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;
    private long id;
    protected String title;
    protected List<Player> players;
    protected int runState;
    protected TSettings settings;
    protected List<TScore> scores;

    public Game(String title, List<Player> players, TSettings settings) {
        this.title = title;
        this.players = players;
        this.settings = settings;
        id = UUID.randomUUID().getMostSignificantBits();
        runState = STATE_INITIALIZED;
        scores = new ArrayList<>();
    }

    public void addScore(TScore score) {
        this.scores.add(score);
    }

    public TScore removeLastScore() {
        int lastIndex = this.scores.size()-1;
        if (lastIndex < 0) {
            return null;
        }
        return this.scores.remove(lastIndex);
    }

    public void updateScore(TScore score) {
        this.scores.set(score.getIndex(), score);
    }

    public List<TScore> getScores() {
        return scores;
    }

    public void setSettings(TSettings settings) {
        this.settings = settings;
    }

    public TSettings getSettings() {
        return settings;
    }

    public void start() {
        runState = STATE_RUNNING;
    }

    public void finish() {
        runState = STATE_FINISHED;
    }

    public boolean isRunning() {
        return runState == STATE_RUNNING;
    }

    public boolean isFinished() {
        return runState == STATE_FINISHED;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public abstract static class Builder<T extends Game<S, C>,
            S extends GameSettings, C extends Score> {
        protected String title;
        protected List<Player> players;
        protected S settings;

        public abstract T build();

        public Builder<T, S, C> fromCommand(GameCommand<S> command) {
            this.title = command.getTitle();
            this.players = new ArrayList<>();
            for (int j = 1; j <= command.getNumberOfPlayers(); j++) {
                players.add(new Player("Player " + j));
            }
            players = Collections.unmodifiableList(players);
            this.settings = command.getSettings();
            return this;
        }
    }
}
