package com.damhoe.scoresheetskat.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;

import javax.inject.Inject;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

   final LoadGameUseCase mLoadGameUseCase;
   final CreateGameUseCase mCreateGameUseCase;

   @Inject
   public HomeViewModelFactory(
           LoadGameUseCase loadGameUseCase,
           CreateGameUseCase createGameUseCase
   ) {
      mLoadGameUseCase = loadGameUseCase;
      mCreateGameUseCase = createGameUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == HomeViewModel.class) {
         return (T) new HomeViewModel(mLoadGameUseCase, mCreateGameUseCase);
      }
      throw new IllegalArgumentException("HomeViewModelFactory needs to be called with type HomeViewModel class.");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == HomeViewModel.class) {
         return (T) new HomeViewModel(mLoadGameUseCase, mCreateGameUseCase);
      }
      throw new IllegalArgumentException("HomeViewModelFactory needs to be called with type HomeViewModel class.");
   }
}
