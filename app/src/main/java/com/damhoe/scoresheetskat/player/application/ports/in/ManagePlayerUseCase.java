package com.damhoe.scoresheetskat.player.application.ports.in;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.domain.Player;

public interface ManagePlayerUseCase {
 Result<Player> createPlayer(String name);
 Result<Boolean> renamePlayer(long id, String name);
 Result<Player> deletePlayer(Player player);
 Result<Boolean> checkIfNameExists(String name);
}
