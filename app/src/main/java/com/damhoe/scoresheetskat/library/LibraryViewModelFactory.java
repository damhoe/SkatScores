package com.damhoe.scoresheetskat.library;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;

import javax.inject.Inject;

public class LibraryViewModelFactory implements ViewModelProvider.Factory {

   final LoadGameUseCase mLoadGameUseCase;
   final CreateGameUseCase mCreateGameUseCase;

   @Inject
   public LibraryViewModelFactory(
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
      if (modelClass == LibraryViewModel.class) {
         return (T) new LibraryViewModel(mLoadGameUseCase, mCreateGameUseCase);
      }
      throw new IllegalArgumentException("LibraryViewModelFactory needs to be called with type LibraryViewModel class.");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == LibraryViewModel.class) {
         return (T) new LibraryViewModel(mLoadGameUseCase, mCreateGameUseCase);
      }
      throw new IllegalArgumentException("LibraryViewModelFactory needs to be called with type LibraryViewModel class.");
   }
}
