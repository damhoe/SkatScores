package com.damhoe.scoresheetskat.game.adapter.in.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;

import javax.inject.Inject;

class SelectPlayerVMFactory implements ViewModelProvider.Factory {

   private final GetPlayersUseCase getPlayersUseCase;
   private final ManagePlayerUseCase managePlayerUseCase;

   @Inject
   SelectPlayerVMFactory(GetPlayersUseCase getPlayersUseCase,
                         ManagePlayerUseCase managePlayerUseCase) {
      this.getPlayersUseCase = getPlayersUseCase;
      this.managePlayerUseCase = managePlayerUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == SelectPlayerViewModel.class) {
         return (T) new SelectPlayerViewModel(managePlayerUseCase, getPlayersUseCase);
      }
      throw new RuntimeException("This factory required SelectPlayerViewModel class");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == SelectPlayerViewModel.class) {
         return (T) new SelectPlayerViewModel(managePlayerUseCase, getPlayersUseCase);
      }
      throw new RuntimeException("This factory required SelectPlayerViewModel class");
   }
}
