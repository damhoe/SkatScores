package com.damhoe.scoresheetskat.game.adapter.in.ui;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.application.ports.in.AddScoreToGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.CreateGameUseCase;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;
import com.damhoe.scoresheetskat.game_setup.domain.GameCommand;
import com.damhoe.scoresheetskat.game_setup.domain.SkatGameCommand;
import com.damhoe.scoresheetskat.player.domain.Player;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

public class SkatGameViewModel extends GameViewModel<SkatGame, SkatSettings, SkatScore> {

   private final CreateGameUseCase createGameUseCase;
   private final LoadGameUseCase loadGameUseCase;
   private final AddScoreToGameUseCase addScoreToGameUseCase;

   private final MediatorLiveData<Pair<Integer, Integer>> currentRoundInfo = new MediatorLiveData<>();

   public final LiveData<int[]> totalPoints = Transformations.map(getGame(), SkatGame::calculateTotalPoints);
   public final LiveData<int[]> winBonus = Transformations.map(getGame(), SkatGame::calculateWinBonus);
   public final LiveData<int[]> lossOfOthersBonus = Transformations.map(getGame(), SkatGame::calculateLossOfOthersBonus);

   final LiveData<Player> dealer = Transformations.map(getGame(), skatGame -> {
      int firstDealerPosition = skatGame.getFirstDealerPosition();
      int currentRound = skatGame.getCurrentRound();
      int playerCount = skatGame.getPlayers().size();
      int dealerPosition = (firstDealerPosition - 1 + currentRound) % playerCount;
      return skatGame.getPlayers().get(dealerPosition);
   });

   public SkatGameViewModel(CreateGameUseCase createGameUseCase,
                            LoadGameUseCase loadGameUseCase,
                            AddScoreToGameUseCase addScoreToGameUseCase) {
      this.createGameUseCase = createGameUseCase;
      this.loadGameUseCase = loadGameUseCase;
      this.addScoreToGameUseCase = addScoreToGameUseCase;
      currentRoundInfo.addSource(getGame(), game -> {
         if (game != null) {
            int currentRound = game.getCurrentRound();
            int totalRounds = game.getSettings().getNumberOfRounds();
            this.currentRoundInfo.setValue(new Pair<>(currentRound, totalRounds));
         }
      });
   }

   public LiveData<Pair<Integer, Integer>> getCurrentRoundInfo() {
      return currentRoundInfo;
   }

   @Override
   public void initialize(long gameId) {
      SkatGame game = loadGameUseCase.getGame(gameId).getValue();
      game.start();
      setGame(game);
   }

   @Override
   public <TCommand extends GameCommand<SkatSettings>> void initialize(TCommand command) {
      if (command.getClass() != SkatGameCommand.class) {
         throw new IllegalArgumentException(
                 "Expected SkatGameCommand but got: " + command.getClass());
      }
      Result<SkatGame> result = createGameUseCase.createSkatGame((SkatGameCommand) command);
      if (result.isFailure()) {
         // TODO
      }
      SkatGame game = result.getValue();
      game.start();
      setGame(game);
   }

   @Override
   public void notifyGameIsFinished() {
      // TODO
   }

   @Override
   public void addScore(SkatScore score) {
      SkatGame skatGame = getGame().getValue();
      addScoreToGameUseCase.addScoreToGame(skatGame, score);
      setGame(skatGame);
   }

   @Override
   public Result<SkatScore> removeLastScore() {
      SkatGame skatGame = getGame().getValue();
      Result<SkatScore> result = addScoreToGameUseCase.removeLastScore(skatGame);
      if (result.isSuccess()) {
         setGame(skatGame);
      }
      return result;
   }

   @Override
   public void updateScore(SkatScore score) {
      SkatGame skatGame = getGame().getValue();
      assert skatGame != null;
      skatGame.updateScore(score);
      setGame(skatGame);
   }

   @Override
   public boolean isInitialized() {
      return getGame().getValue() != null;
   }
}
