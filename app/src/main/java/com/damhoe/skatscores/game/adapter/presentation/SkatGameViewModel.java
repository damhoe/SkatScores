package com.damhoe.skatscores.game.adapter.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.domain.skat.SkatGameLegacy;
import com.damhoe.skatscores.game.domain.skat.application.ports.AddScoreToGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.CrudSkatGameUseCase;
import com.damhoe.skatscores.game.domain.skat.application.ports.LoadSkatGameUseCase;
import com.damhoe.skatscores.game.domain.GameRunStateInfo;
import com.damhoe.skatscores.game.domain.skat.SkatSettings;
import com.damhoe.skatscores.game.domain.GameCommand;
import com.damhoe.skatscores.game.domain.skat.SkatGameCommand;
import com.damhoe.skatscores.game.domain.score.SkatScore;

public class SkatGameViewModel extends GameViewModel<SkatGameLegacy, SkatSettings, SkatScore> {

   private final MediatorLiveData<GameRunStateInfo> runStateInfo = new MediatorLiveData<>();

   public final LiveData<int[]> totalPoints = Transformations.map(getGame(), SkatGameLegacy::calculateTotalPoints);
   public final LiveData<int[]> winBonus = Transformations.map(getGame(), SkatGameLegacy::calculateWinBonus);
   public final LiveData<int[]> lossOfOthersBonus = Transformations.map(getGame(), SkatGameLegacy::calculateLossOfOthersBonus);

   final LiveData<Integer> dealerPosition = Transformations.map(getGame(), skatGame -> {
      int firstDealerPosition = skatGame.getStartDealerPosition();
      int currentRound = skatGame.getCurrentRound();
      int playerCount = skatGame.getPlayers().size();
      return (firstDealerPosition - 1 + currentRound) % playerCount;
   });

   public SkatGameViewModel(
           CrudSkatGameUseCase createGameUseCase,
           LoadSkatGameUseCase loadSkatGameUseCase,
           AddScoreToGameUseCase addScoreToGameUseCase
   ) {
      super(createGameUseCase, loadSkatGameUseCase, addScoreToGameUseCase);
      runStateInfo.addSource(getGame(), game -> {
         int currentRound = game.getCurrentRound();
         int totalRounds = game.settings.roundCount;
         runStateInfo.postValue(new GameRunStateInfo(totalRounds, currentRound, game.isFinished()));
      });
   }

   public LiveData<GameRunStateInfo> getGameRunStateInfo() {
      return runStateInfo;
   }

   @Override
   public void initialize(long gameId) {
      SkatGameLegacy game = mLoadSkatGameUseCase.getGame(gameId).value;
      setGame(game);
   }

   @Override
   public <TCommand extends GameCommand<SkatSettings>> void initialize(TCommand command) {
      if (command.getClass() != SkatGameCommand.class) {
         throw new IllegalArgumentException(
                 "Expected SkatGameCommand but got: " + command.getClass());
      }
      Result<SkatGameLegacy> result = mCreateGameUseCase.createSkatGame((SkatGameCommand) command);
      if (result.isFailure()) {
         // TODO
      }
      SkatGameLegacy game = result.value;
      game.start();
      setGame(game);
   }

   @Override
   public void notifyGameIsFinished() {
      // TODO
   }

   @Override
   public void addScore(SkatScore score) {
      SkatGameLegacy skatGame = getGame().getValue();
      mAddScoreToGameUseCase.addScoreToGame(skatGame, score);
      setGame(skatGame);
   }

   @Override
   public Result<SkatScore> removeLastScore() {
      SkatGameLegacy skatGame = getGame().getValue();
      Result<SkatScore> result = mAddScoreToGameUseCase.removeLastScore(skatGame);
      if (result.isSuccess()) {
         setGame(skatGame);
      }
      return result;
   }

   @Override
   public void updateScore(SkatScore score) {
      SkatGameLegacy skatGame = getGame().getValue();
      assert skatGame != null;
      skatGame.updateScore(score);
      setGame(skatGame);
   }

   @Override
   public boolean isInitialized() {
      return getGame().getValue() != null;
   }
}
