package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

class SelectPlayerViewModel extends ViewModel {
   private final GetPlayerUseCase getPlayerUseCase;


   SelectPlayerViewModel(GetPlayerUseCase getPlayerUseCase) {
      this.getPlayerUseCase = getPlayerUseCase;
   }

   protected List<Player> getAllPlayers() {
      return getPlayerUseCase.getPlayers();
   }

   protected Result<Player> findOrCreatePlayer(String name) {
      return getPlayerUseCase.findOrCreate(name);
   }
}
