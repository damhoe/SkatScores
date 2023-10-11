package com.damhoe.scoresheetskat.player.application.ports.in;

import com.damhoe.scoresheetskat.base.SearchResult;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

public interface GetPlayersUseCase {
 Player getPlayer(long id);
 SearchResult<Player> searchForPlayersWithName(String name);
 List<Player> loadAll();
}
