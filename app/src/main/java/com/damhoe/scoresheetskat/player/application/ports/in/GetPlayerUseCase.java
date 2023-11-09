package com.damhoe.scoresheetskat.player.application.ports.in;

import androidx.lifecycle.LiveData;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

public interface GetPlayerUseCase {
 LiveData<List<Player>> getPlayersLiveData();
 List<Player> getPlayers();
 Result<Player> getPlayer(long id);
 Result<Player> findOrCreate(String name);
 Result<Player> createPlayer(String name);
 Result<Player> deletePlayer(long id);
 boolean isNameExistent(String name);
 void refreshPlayers();
}
