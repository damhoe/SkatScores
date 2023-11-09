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

import java.util.List;

public class SkatGameViewModel extends GameViewModel<SkatGame, SkatSettings, SkatScore> {

   private final MediatorLiveData<Pair<Integer, Integer>> currentRoundInfo = new MediatorLiveData<>();

   public final LiveData<int[]> totalPoints = Transformations.map(getGame(), SkatGame::calculateTotalPoints);
   public final LiveData<int[]> winBonus = Transformations.map(getGame(), SkatGame::calculateWinBonus);
   public final LiveData<int[]> lossOfOthersBonus = Transformations.map(getGame(), SkatGame::calculateLossOfOthersBonus);

   final LiveData<Integer> dealerPosition = Transformations.map(getGame(), skatGame -> {
      int firstDealerPosition = skatGame.getStartDealerPosition();
      int currentRound = skatGame.getCurrentRound();
      int playerCount = skatGame.getPlayers().size();
      return (firstDealerPosition - 1 + currentRound) % playerCount;
   });

   public SkatGameViewModel(
           CreateGameUseCase createGameUseCase,
           LoadGameUseCase loadGameUseCase,
           AddScoreToGameUseCase addScoreToGameUseCase
   ) {
      super(createGameUseCase, loadGameUseCase, addScoreToGameUseCase);
      currentRoundInfo.addSource(getGame(), game -> {
         if (game != null) {
            int currentRound = game.getCurrentRound();
            int totalRounds = game.getSettings().getNumberOfRounds();
            this.currentRoundInfo.setValue(new Pair<>(currentRound, totalRounds));
         }
      });
   }

   @Override
   public void updatePlayers(List<Player> players) {
      SkatGame game = getGame().getValue();
      if (game != null) {
         game.setPlayers(players);
         setGame(game);
      }

      // Persist changes
      mCreateGameUseCase.updateSkatGame(game);
   }

   public LiveData<Pair<Integer, Integer>> getCurrentRoundInfo() {
      return currentRoundInfo;
   }

   @Override
   public void initialize(long gameId) {
      SkatGame game = mLoadGameUseCase.getGame(gameId).getValue();
      game.start();
      setGame(game);
   }

   @Override
   public <TCommand extends GameCommand<SkatSettings>> void initialize(TCommand command) {
      if (command.getClass() != SkatGameCommand.class) {
         throw new IllegalArgumentException(
                 "Expected SkatGameCommand but got: " + command.getClass());
      }
      Result<SkatGame> result = mCreateGameUseCase.createSkatGame((SkatGameCommand) command);
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
      mAddScoreToGameUseCase.addScoreToGame(skatGame, score);
      setGame(skatGame);
   }

   @Override
   public Result<SkatScore> removeLastScore() {
      SkatGame skatGame = getGame().getValue();
      Result<SkatScore> result = mAddScoreToGameUseCase.removeLastScore(skatGame);
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
