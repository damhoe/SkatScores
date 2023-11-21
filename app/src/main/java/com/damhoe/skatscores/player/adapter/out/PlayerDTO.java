package com.damhoe.skatscores.player.adapter.out;

import com.damhoe.skatscores.player.domain.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PlayerDTO {
    private long id;
    private String name;
    private String createdAt;
    private String updatedAt;

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player asPlayer(int gameCount) {
        Player player = new Player("");
        player.setID(getId());
        player.setName(getName());
        player.setGameCount(gameCount);
        try {
            player.setCreatedAt(createSimpleDateFormat().parse(getCreatedAt()));
            player.setUpdatedAt(createSimpleDateFormat().parse(getUpdatedAt()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player;
    }

    public static PlayerDTO fromPlayer(Player player) {
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setId(player.getId());
        playerDTO.setName(player.getName());
        playerDTO.setCreatedAt(getCurrentTimeStamp());
        playerDTO.setUpdatedAt(getCurrentTimeStamp());
        return playerDTO;
    }

    private static SimpleDateFormat createSimpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    }

    private static String getCurrentTimeStamp() {
        return createSimpleDateFormat().format(Calendar.getInstance().getTime());
    }
}
