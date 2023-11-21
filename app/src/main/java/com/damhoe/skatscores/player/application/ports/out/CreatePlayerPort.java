package com.damhoe.skatscores.player.application.ports.out;

import com.damhoe.skatscores.player.domain.Player;

public interface CreatePlayerPort {
 Player createPlayer(String name);
 Player deletePlayer(long id);
}
