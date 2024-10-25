package com.damhoe.skatscores.library;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase;

import javax.inject.Inject;

public class LibraryViewModelFactory implements ViewModelProvider.Factory {

   final LoadSkatGameUseCase mLoadSkatGameUseCase;
   final CrudSkatGameUseCase mCreateGameUseCase;

   @Inject
   public LibraryViewModelFactory(
           LoadSkatGameUseCase loadSkatGameUseCase,
           CrudSkatGameUseCase createGameUseCase
   ) {
      mLoadSkatGameUseCase = loadSkatGameUseCase;
      mCreateGameUseCase = createGameUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == LibraryViewModel.class) {
         return (T) new LibraryViewModel(mLoadSkatGameUseCase, mCreateGameUseCase);
      }
      throw new IllegalArgumentException("LibraryViewModelFactory needs to be called with type LibraryViewModel class.");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == LibraryViewModel.class) {
         return (T) new LibraryViewModel(mLoadSkatGameUseCase, mCreateGameUseCase);
      }
      throw new IllegalArgumentException("LibraryViewModelFactory needs to be called with type LibraryViewModel class.");
   }
}
