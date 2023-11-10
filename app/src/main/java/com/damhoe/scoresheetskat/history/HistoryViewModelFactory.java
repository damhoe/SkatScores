package com.damhoe.scoresheetskat.history;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;

import javax.inject.Inject;

public class HistoryViewModelFactory implements ViewModelProvider.Factory {

   private final LoadGameUseCase mLoadGameUseCase;
   private final CreateGameUseCase mCreateGameUseCase;

   @Inject
   public HistoryViewModelFactory(
           LoadGameUseCase loadGameUseCase,
           CreateGameUseCase createGameUseCase
   ) {
      mLoadGameUseCase = loadGameUseCase;
      mCreateGameUseCase = createGameUseCase;
   }


   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == HistoryViewModel.class) {
         return (T) new HistoryViewModel(mLoadGameUseCase, mCreateGameUseCase);
      }
      throw new RuntimeException("Invalid model class for HistoryViewModelFactory: " + modelClass);
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass,
                                         @NonNull CreationExtras extras) {
      if (modelClass == HistoryViewModel.class) {
         return (T) new HistoryViewModel(mLoadGameUseCase, mCreateGameUseCase);
      }
      throw new RuntimeException("Invalid model class for HistoryViewModelFactory: " + modelClass);
   }
}
