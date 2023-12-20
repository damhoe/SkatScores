package com.damhoe.skatscores.game.game_home.adapter.in.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.skatscores.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.skatscores.player.application.ports.in.UpdatePlayerUseCase;

import javax.inject.Inject;

class SelectPlayerVMFactory implements ViewModelProvider.Factory {

   private final GetPlayerUseCase getPlayerUseCase;
   private final UpdatePlayerUseCase updatePlayerUseCase;

   @Inject
   SelectPlayerVMFactory(GetPlayerUseCase getPlayerUseCase,
                         UpdatePlayerUseCase updatePlayerUseCase) {
      this.getPlayerUseCase = getPlayerUseCase;
      this.updatePlayerUseCase = updatePlayerUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == SelectPlayerViewModel.class) {
         return (T) new SelectPlayerViewModel(getPlayerUseCase);
      }
      throw new RuntimeException("This factory required SelectPlayerViewModel class");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == SelectPlayerViewModel.class) {
         return (T) new SelectPlayerViewModel(getPlayerUseCase);
      }
      throw new RuntimeException("This factory required SelectPlayerViewModel class");
   }
}
