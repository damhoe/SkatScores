package com.damhoe.scoresheetskat.game.adapter.out.models;

public class SkatGameDTO {
    private long id;
    private String title;
    private String createdAt;
    private String updatedAt;
    private int startDealerPosition;
    private long settingsId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStartDealerPosition() {
        return startDealerPosition;
    }

    public void setStartDealerPosition(int startDealerPosition) {
        this.startDealerPosition = startDealerPosition;
    }

    public long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(long settingsId) {
        this.settingsId = settingsId;
    }
}
