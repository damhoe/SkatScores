package com.damhoe.skatscores.game.application;

import com.damhoe.skatscores.score.Constant;
import com.damhoe.skatscores.score.domain.SkatScore;

import java.util.List;

public final class TotalPointsCalculator {
   public static int[] calculateTotalPoints(int numberOfPlayers,
                                            List<SkatScore> scores,
                                            boolean isTournamentScoring) {
      int[] points = new int[numberOfPlayers];
      for (int k = 0; k < scores.size(); k++) {
         SkatScore score = scores.get(k);
         if (score.isPasse())
            continue;
         int scorePoints = score.toPoints();
         points[score.getPlayerPosition()] += scorePoints;
      }
      if (isTournamentScoring) {
         int[] pointsLossOfOthers = calculateLossOfOthersBonus(numberOfPlayers, scores);
         int[] pointsWin = calculateWinBonus(numberOfPlayers, scores);
         for (int i = 0; i < numberOfPlayers; i++) {
            points[i] += pointsLossOfOthers[i] + pointsWin[i];
         }
      }
      return points;
   }

   public static int[] calculateLossOfOthersBonus(int numberOfPlayers, List<SkatScore> scores) {
      int[] points = new int[numberOfPlayers];
      for (int k = 0; k < scores.size(); k++) {
         SkatScore score = scores.get(k);
         if (score.isPasse()) {
            continue;
         }
         if (score.isWon()) {
            continue;
         }
         for (int j = 0; j < numberOfPlayers; j++) {
            if (j != score.getPlayerPosition()) {
               points[j] += Constant.BONUS_LOSS_OTHERS;
            }
         }
      }
      return points;
   }

   public static int[] calculateWinBonus(int numberOfPlayers, List<SkatScore> scores) {
      int[] points = new int[numberOfPlayers];
      for (int k = 0; k < scores.size(); k++) {
         SkatScore score = scores.get(k);
         if (score.isPasse()) {
            continue;
         }
         int bonus = Constant.BONUS_WIN;
         if (!score.isWon()) {
            bonus *= -1;
         }
         points[score.getPlayerPosition()] += bonus;
      }
      return points;
   }
}
