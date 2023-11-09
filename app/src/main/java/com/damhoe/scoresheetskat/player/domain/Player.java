package com.damhoe.scoresheetskat.player.domain;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.damhoe.scoresheetskat.R;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Player {

    public static final String DUMMY_PATTERN = "^(Player|Spieler)(\\s\\d+|in\\s\\d+)$";

    private long ID;
    private String name;
    private Date createdAt;
    private Date updatedAt;
    private int gameCount;

    public Player(String mName) {
        ID = UUID.randomUUID().getMostSignificantBits();
        name = mName;
        createdAt = Calendar.getInstance().getTime();
        updatedAt = Calendar.getInstance().getTime();
        gameCount = 0;
    }

    public String getName() {
        return name;
    }

    public long getID() {
        return ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public boolean isDummy() {
        return name.matches(DUMMY_PATTERN);
    }

    public static boolean isDummyName(String name) {
        return name.matches(DUMMY_PATTERN);
    }

    public static Player createDummy(int number) {
        String name = Resources.getSystem().getString(R.string.dummy_player_name);
        return new Player(name + " " + number);
    }

    public static Player createDummy(String name) {
        if (!isDummyName(name)) {
            throw new IllegalArgumentException(
                    "It is not allowed to create dummy player with custom names: " + name);
        }
        return new Player(name);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
