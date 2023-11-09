package com.damhoe.scoresheetskat.player.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.domain.Player;

public interface UpdatePlayerUseCase {
 Result<Player> renamePlayer(long id, String name);
}
