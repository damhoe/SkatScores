package com.damhoe.skatscores.player.application.ports.out;

import com.damhoe.skatscores.player.domain.Player;

public interface UpdatePlayerPort {
 Player updateName(long id, String newName);
}
