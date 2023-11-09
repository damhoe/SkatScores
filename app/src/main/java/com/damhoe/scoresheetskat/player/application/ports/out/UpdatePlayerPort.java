package com.damhoe.scoresheetskat.player.application.ports.out;

import com.damhoe.scoresheetskat.player.domain.Player;

public interface UpdatePlayerPort {
 Player updateName(long id, String newName);
}
