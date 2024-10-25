package com.damhoe.skatscores.game.adapter.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.skatscores.game.domain.skat.application.ports.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase;

import javax.inject.Inject;

public class GameViewModelFactory implements ViewModelProvider.Factory {
   private final CrudSkatGameUseCase createGameUseCase;
   private final LoadSkatGameUseCase loadSkatGameUseCase;
   private final AddScoreToGameUseCase addScoreToGameUseCase;

   @Inject
   public GameViewModelFactory(CrudSkatGameUseCase createGameUseCase,
                               LoadSkatGameUseCase loadSkatGameUseCase,
                               AddScoreToGameUseCase addScoreToGameUseCase) {
      this.createGameUseCase = createGameUseCase;
      this.loadSkatGameUseCase = loadSkatGameUseCase;
      this.addScoreToGameUseCase = addScoreToGameUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == SkatGameViewModel.class) {
         return (T) new SkatGameViewModel(createGameUseCase, loadSkatGameUseCase, addScoreToGameUseCase);
      }
      throw new IllegalArgumentException(
              "Only SkatGameViewModel allowed in GameFragment ViewModel factory method.");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == SkatGameViewModel.class) {
         return (T) new SkatGameViewModel(createGameUseCase, loadSkatGameUseCase, addScoreToGameUseCase);
      }
      throw new IllegalArgumentException(
              "Only SkatGameViewModel allowed in GameFragment ViewModel factory method.");
   }
}
