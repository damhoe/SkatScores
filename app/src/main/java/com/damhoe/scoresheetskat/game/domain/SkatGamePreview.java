package com.damhoe.scoresheetskat.game.domain;

import java.util.Date;
import java.util.List;

public class SkatGamePreview {
   private String title;
   private Date date;
   private List<String> playerNames;
   private long gameId;
   private GameRunStateInfo gameRunStateInfo;

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public Date getDate() {
      return date;
   }

   public void setDate(Date date) {
      this.date = date;
   }

   public List<String> getPlayerNames() {
      return playerNames;
   }

   public void setPlayerNames(List<String> playerNames) {
      this.playerNames = playerNames;
   }

   public long getGameId() {
      return gameId;
   }

   public void setGameId(long gameId) {
      this.gameId = gameId;
   }

   public GameRunStateInfo getProgressInfo() {
      return gameRunStateInfo;
   }

   public void setProgressInfo(GameRunStateInfo gameRunStateInfo) {
      this.gameRunStateInfo = gameRunStateInfo;
   }
}
