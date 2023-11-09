package com.damhoe.scoresheetskat.player.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.UpdatePlayerUseCase;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

class PlayerViewModel extends ViewModel {

    private final UpdatePlayerUseCase updatePlayerUseCase;
    private final GetPlayerUseCase getPlayerUseCase;

    private final MutableLiveData<Player> selectedPlayer = new MutableLiveData<>();

    PlayerViewModel(
            UpdatePlayerUseCase updatePlayerUseCase,
            GetPlayerUseCase getPlayerUseCase
    ) {
        this.updatePlayerUseCase = updatePlayerUseCase;
        this.getPlayerUseCase = getPlayerUseCase;
    }

    LiveData<List<Player>> getPlayers() {
        return getPlayerUseCase.getPlayersLiveData();
    }

    LiveData<Player> getSelectedPlayer() {
        return selectedPlayer;
    }

    public void selectPlayer(Player player) {
        selectedPlayer.postValue(player);
    }

    public Result<Player> addPlayer(String name) {
        return getPlayerUseCase.createPlayer(name);
    }

    public Result<Player> removePlayer(Player player) {
        return getPlayerUseCase.deletePlayer(player.getID());
    }

    public boolean isExistentName(String name) {
        return getPlayerUseCase.isNameExistent(name);
    }

    public Result<Player> updatePlayer(Player player) {
        return updatePlayerUseCase.renamePlayer(player.getID(), player.getName());
    }
}