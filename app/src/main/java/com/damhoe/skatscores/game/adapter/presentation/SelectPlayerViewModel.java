package com.damhoe.skatscores.game.adapter.presentation;

import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.skatscores.player.domain.Player;

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
