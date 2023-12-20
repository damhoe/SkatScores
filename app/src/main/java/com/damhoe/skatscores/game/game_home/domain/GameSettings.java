package com.damhoe.skatscores.game.game_home.domain;

import android.os.Parcelable;

public abstract class GameSettings implements Parcelable {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
