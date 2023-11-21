package com.damhoe.skatscores.player.application.ports.in;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.player.domain.Player;

public interface UpdatePlayerUseCase {
 Result<Player> renamePlayer(long id, String name);
}
