package com.damhoe.scoresheetskat.player.application.ports.out;

import com.damhoe.scoresheetskat.base.SearchResult;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

public interface LoadPlayerPort {
 Player loadPlayer(long id);
 List<Player> loadAllPlayers();
 SearchResult<Player> searchByName(String name);
}
