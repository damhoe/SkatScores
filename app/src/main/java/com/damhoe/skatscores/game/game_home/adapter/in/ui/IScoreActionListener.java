package com.damhoe.skatscores.game.game_home.adapter.in.ui;

import com.damhoe.skatscores.game.score.domain.SkatScore;

public interface IScoreActionListener {
 void notifyDelete();
 void notifyDetails(SkatScore skatScore);
 void notifyEdit(SkatScore skatScore);
}
