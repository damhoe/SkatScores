package com.damhoe.scoresheetskat.game_setup.domain;

import android.os.Parcelable;

import com.damhoe.scoresheetskat.game.domain.GameSettings;

public abstract class GameCommand<T extends GameSettings> implements Parcelable {
   protected String title;
   protected int numberOfPlayers;
   protected T settings;

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public int getNumberOfPlayers() {
      return numberOfPlayers;
   }

   public void setNumberOfPlayers(int numberOfPlayers) {
      this.numberOfPlayers = numberOfPlayers;
   }

   public T getSettings() {
      return settings;
   }

   public void setSettings(T settings) {
      this.settings = settings;
   }

   public abstract boolean isValid();
}
