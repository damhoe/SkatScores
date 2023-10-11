package com.damhoe.scoresheetskat.game.adapter.in.ui;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

public interface IScoreActionListener {
 void notifyDelete();
 void notifyDetails(SkatScore skatScore);
 void notifyEdit(SkatScore skatScore);
}
