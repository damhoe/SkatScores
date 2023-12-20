package com.damhoe.skatscores.player.adapter.in.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.skatscores.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.skatscores.player.application.ports.in.UpdatePlayerUseCase;

import javax.inject.Inject;

public class PlayerViewModelFactory implements ViewModelProvider.Factory {

   private final UpdatePlayerUseCase updatePlayerUseCase;
   private final GetPlayerUseCase getPlayerUseCase;

   @Inject
   PlayerViewModelFactory(UpdatePlayerUseCase updatePlayerUseCase, GetPlayerUseCase getPlayerUseCase) {
      this.updatePlayerUseCase = updatePlayerUseCase;
      this.getPlayerUseCase = getPlayerUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == PlayerViewModel.class) {
         return (T) new PlayerViewModel(updatePlayerUseCase, getPlayerUseCase);
      }
      throw new RuntimeException("PlayerViewModelFactory create() called with invalid model class: " + modelClass);
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == PlayerViewModel.class) {
         return (T) new PlayerViewModel(updatePlayerUseCase, getPlayerUseCase);
      }
      throw new RuntimeException("PlayerViewModelFactory create() called with invalid model class: " + modelClass);
   }
}
