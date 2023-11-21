package com.damhoe.skatscores.game.adapter.in.ui;

import com.damhoe.skatscores.score.domain.SkatScore;

public interface IScoreActionListener {
 void notifyDelete();
 void notifyDetails(SkatScore skatScore);
 void notifyEdit(SkatScore skatScore);
}
