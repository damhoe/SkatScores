package com.damhoe.skatscores.game.game_home.domain;

import com.damhoe.skatscores.game.game_setup.domain.GameCommand;
import com.damhoe.skatscores.game.score.domain.Score;
import com.damhoe.skatscores.player.domain.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class Game<TSettings extends GameSettings, TScore extends Score> {

    public enum RunState {
        RUNNING,
        FINISHED
    }

    private long id;
    protected Date createdAt;
    protected String title;
    protected List<Player> players;
    protected RunState runState;
    protected TSettings settings;
    protected List<TScore> scores;

    public Game(String title, List<Player> players, TSettings settings) {
        this.title = title;
        this.players = players;
        this.settings = settings;
        id = UUID.randomUUID().getMostSignificantBits();
        createdAt = Calendar.getInstance().getTime();
        scores = new ArrayList<>();
        start();
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date date) {
        this.createdAt = date;
    }

    public void updateScore(TScore score) {
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).getId() == score.getId()) {
                scores.set(i, score);
                break;
            }
        }
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
        runState = RunState.RUNNING;
    }

    public void finish() {
        runState = RunState.FINISHED;
    }

    public void resume() {
        runState = RunState.RUNNING;
    }

    public boolean isFinished() {
        return runState == RunState.FINISHED;
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
        protected String mTitle;
        protected List<Player> mPlayers;
        protected S mSettings;

        public abstract T build();

        public Builder<T, S, C> fromCommand(GameCommand<S> command) {
            mTitle = command.getTitle();
            mPlayers = new ArrayList<>();
            for (int j = 1; j <= command.getNumberOfPlayers(); j++) {
                mPlayers.add(new Player("Player " + j));
            }
            mPlayers = Collections.unmodifiableList(mPlayers);
            mSettings = command.getSettings();
            return this;
        }

        public Builder<T, S, C> setSettings(S settings) {
            mSettings = settings;
            return this;
        }

        public Builder<T, S, C> setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder<T, S, C> setPlayers(List<Player> players) {
            mPlayers = players;
            return this;
        }
    }
}
