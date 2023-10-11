package com.damhoe.scoresheetskat.player.adapter.in.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.LoadPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;

import javax.inject.Inject;

class PlayersViewModelFactory implements ViewModelProvider.Factory {

   private final ManagePlayerUseCase managePlayerUseCase;
   private final GetPlayersUseCase getPlayersUseCase;

   @Inject
   PlayersViewModelFactory(ManagePlayerUseCase managePlayerUseCase, GetPlayersUseCase getPlayersUseCase) {
      this.managePlayerUseCase = managePlayerUseCase;
      this.getPlayersUseCase = getPlayersUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == PlayersViewModel.class) {
         return (T) new PlayersViewModel(managePlayerUseCase, getPlayersUseCase);
      }
      throw new RuntimeException("PlayersViewModelFactory create() called with invalid model class: " + modelClass);
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == PlayersViewModel.class) {
         return (T) new PlayersViewModel(managePlayerUseCase, getPlayersUseCase);
      }
      throw new RuntimeException("PlayersViewModelFactory create() called with invalid model class: " + modelClass);
   }
}
