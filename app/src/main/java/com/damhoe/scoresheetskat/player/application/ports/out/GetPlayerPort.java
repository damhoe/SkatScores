package com.damhoe.scoresheetskat.player.application.ports.out;

import androidx.lifecycle.LiveData;

import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

public interface GetPlayerPort {
 Player getPlayer(long id);
 LiveData<List<Player>> getPlayersLiveData();
 List<Player> getPlayers();
 void refreshPlayersFromRepository();
}
