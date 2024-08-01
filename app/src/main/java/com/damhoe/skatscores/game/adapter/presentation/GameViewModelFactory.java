package com.damhoe.skatscores.game.adapter.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.damhoe.skatscores.game.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.application.ports.in.CreateGameUseCase;
import com.damhoe.skatscores.game.application.ports.in.LoadGameUseCase;

import javax.inject.Inject;

public class GameViewModelFactory implements ViewModelProvider.Factory {
   private final CreateGameUseCase createGameUseCase;
   private final LoadGameUseCase loadGameUseCase;
   private final AddScoreToGameUseCase addScoreToGameUseCase;

   @Inject
   public GameViewModelFactory(CreateGameUseCase createGameUseCase,
                               LoadGameUseCase loadGameUseCase,
                               AddScoreToGameUseCase addScoreToGameUseCase) {
      this.createGameUseCase = createGameUseCase;
      this.loadGameUseCase = loadGameUseCase;
      this.addScoreToGameUseCase = addScoreToGameUseCase;
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (modelClass == SkatGameViewModel.class) {
         return (T) new SkatGameViewModel(createGameUseCase, loadGameUseCase, addScoreToGameUseCase);
      }
      throw new IllegalArgumentException(
              "Only SkatGameViewModel allowed in GameFragment ViewModel factory method.");
   }

   /** @noinspection unchecked*/
   @NonNull
   @Override
   public <T extends ViewModel> T create(@NonNull Class<T> modelClass, @NonNull CreationExtras extras) {
      if (modelClass == SkatGameViewModel.class) {
         return (T) new SkatGameViewModel(createGameUseCase, loadGameUseCase, addScoreToGameUseCase);
      }
      throw new IllegalArgumentException(
              "Only SkatGameViewModel allowed in GameFragment ViewModel factory method.");
   }
}
