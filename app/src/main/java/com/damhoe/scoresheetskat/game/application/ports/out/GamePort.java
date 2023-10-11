package com.damhoe.scoresheetskat.game.application.ports.out;

import com.damhoe.scoresheetskat.game.domain.SkatGame;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

public interface GamePort {
 SkatGame saveSkatGame(SkatGame game);
 boolean addScore(long gameId, SkatScore score);
 SkatGame deleteSkatGame(long id);
 SkatGame loadSkatGame(long id);
}
