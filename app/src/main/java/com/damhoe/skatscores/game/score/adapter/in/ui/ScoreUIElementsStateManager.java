package com.damhoe.skatscores.game.score.adapter.in.ui;

import com.damhoe.skatscores.game.score.domain.SkatScoreCommand;
import com.damhoe.skatscores.game.score.domain.SkatSuit;

class ScoreUIElementsStateManager {
   /**
    * This class is responsible for managing the enabled / disabled states of
    * the UI elements of the score fragment. All state conditions depend upon
    * the state of the score command.
    *
    */

   private SkatScoreCommand scoreCommand;

   public static ScoreUIElementsStateManager fromCommand(SkatScoreCommand scoreCommand) {
      ScoreUIElementsStateManager manager = new ScoreUIElementsStateManager();
      manager.setScoreCommand(scoreCommand);
      return manager;
   }

   public void setScoreCommand(SkatScoreCommand scoreCommand) {
      this.scoreCommand = scoreCommand;
   }

   /**
    * Show results when the game was not passe
    */
   public boolean isResultsEnabled() {
      return !scoreCommand.isPasse();
   }

   /**
    * Show suits when the game was lost or won (not passe / not overbid)
    */
   public boolean isSuitsEnabled() {
      return scoreCommand.isWon() || scoreCommand.isLost();
   }

   public boolean isSpitzenEnabled() {
      return isSuitsEnabled() && scoreCommand.getSuit() != SkatSuit.NULL;
   }

   public boolean isDecreaseSpitzenEnabled() {
      return isSpitzenEnabled() && !scoreCommand.isMinSpitzen();
   }

   public boolean isIncreaseSpitzenEnabled() {
      return isSpitzenEnabled() && !scoreCommand.isMaxSpitzen();
   }

   public boolean isSchneiderSchwarzEnabled() {
      return isSpitzenEnabled();
   }

   public boolean isOuvertEnabled() {
      return isSuitsEnabled();
   }

   public boolean isHandEnabled() {
      return isSuitsEnabled();
   }
}
