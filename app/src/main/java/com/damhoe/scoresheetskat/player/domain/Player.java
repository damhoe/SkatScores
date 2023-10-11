package com.damhoe.scoresheetskat.player.domain;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Player {

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

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
