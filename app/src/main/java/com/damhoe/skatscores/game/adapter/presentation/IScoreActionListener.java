package com.damhoe.skatscores.game.adapter.presentation;

import com.damhoe.skatscores.game.domain.score.SkatScore;

public interface IScoreActionListener {
 void notifyDelete();
 void notifyDetails(SkatScore skatScore);
 void notifyEdit(SkatScore skatScore);
}
