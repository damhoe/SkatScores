package com.damhoe.scoresheetskat.player.application.ports.out;

import com.damhoe.scoresheetskat.player.domain.Player;

public interface CreatePlayerPort {
 boolean isPlayerNameExistent(String name);
 Player createPlayer(String name);
 Player deletePlayer(long id);
}
