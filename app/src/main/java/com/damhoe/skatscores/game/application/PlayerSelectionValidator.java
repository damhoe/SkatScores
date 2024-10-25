package com.damhoe.skatscores.game.application;

import android.util.Pair;

import com.damhoe.skatscores.player.domain.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelectionValidator {
   public enum MessageType {
      Error,
      Warning
   }

   private List<Player> allPlayers;
   private List<String> selectedNames = new ArrayList<>();

   public void initialize(List<Player> allPlayers, List<String> selectedNames) {
      this.selectedNames = selectedNames;
      this.allPlayers = allPlayers;
   }

   public List<Pair<MessageType, String>> validate() {
      List<Pair<MessageType, String>> messages = new ArrayList<>();

      for (String name: selectedNames) {
         if (name.isEmpty()) {
            messages.add(new Pair<>(MessageType.Error, "Valid name is required!"));
            continue;
         }

         long occurrenceCount = selectedNames.stream().filter(s -> s.equals(name)).count();
         if (occurrenceCount >= 2) {
            messages.add(new Pair<>(MessageType.Error, "Player is selected twice!"));
            continue;
         }

         if (isDummyName(name)) {
            messages.add(new Pair<>(null, null));
            continue;
         }

         if (allPlayers.stream().noneMatch(p -> p.name.equals(name))) {
            messages.add(new Pair<>(MessageType.Warning, "New player is created!"));
            continue;
         }

         messages.add(new Pair<>(null, null));
      }

      return messages;
   }

   public boolean isDummyName(String name) {
      return name.matches(Player.DUMMY_PATTERN);
   }

   public void select(int index, String name) {
      this.selectedNames.set(index, name);
   }
}
