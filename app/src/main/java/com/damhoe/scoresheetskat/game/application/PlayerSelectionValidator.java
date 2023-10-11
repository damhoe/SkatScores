package com.damhoe.scoresheetskat.game.application;

import android.util.Pair;

import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerSelectionValidator {
   public enum MessageType {
      Error,
      Warning
   }

   private final List<Player> allPlayers;
   private final List<String> selectedNames = new ArrayList<>();

   public PlayerSelectionValidator(List<Player> allPlayers) {
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

         if (allPlayers.stream().noneMatch(p -> p.getName().equals(name))) {
            messages.add(new Pair<>(MessageType.Warning, "New player is created!"));
            continue;
         }

         messages.add(new Pair<>(null, null));
      }

      return messages;
   }

   public boolean isNewPlayer(String name) {
      return allPlayers.stream().noneMatch(p -> p.getName().equals(name.trim()));
   }

   public Player getPlayer(String name) {
      for (Player player: allPlayers) {
         if (player.getName().equals(name)) {
            return player;
         }
      }
      return null;
   }

   public void initialize(List<String> selectedNames) {
      this.selectedNames.addAll(selectedNames);
   }

   public void select(int index, String name) {
      this.selectedNames.set(index, name);
   }
}
