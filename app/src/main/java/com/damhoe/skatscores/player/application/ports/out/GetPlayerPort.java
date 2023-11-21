package com.damhoe.skatscores.player.application.ports.out;

import androidx.lifecycle.LiveData;

import com.damhoe.skatscores.player.domain.Player;

import java.util.List;

public interface GetPlayerPort {
 Player getPlayer(long id);
 LiveData<List<Player>> getPlayersLiveData();
 List<Player> getPlayers();
 void refreshPlayersFromRepository();
}
