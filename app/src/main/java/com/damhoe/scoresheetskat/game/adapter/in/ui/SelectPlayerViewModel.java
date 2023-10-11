package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

class SelectPlayerViewModel extends ViewModel {
   private final ManagePlayerUseCase managePlayerUseCase;
   private final GetPlayersUseCase getPlayersUseCase;


   SelectPlayerViewModel(ManagePlayerUseCase managePlayerUseCase,
                         GetPlayersUseCase getPlayersUseCase) {
      this.managePlayerUseCase = managePlayerUseCase;
      this.getPlayersUseCase = getPlayersUseCase;
   }

   protected List<Player> getAllPlayers() {
      return getPlayersUseCase.loadAll();
   }

   protected Result<Player> createPlayer(String name) {
      return managePlayerUseCase.createPlayer(name);
   }
}
