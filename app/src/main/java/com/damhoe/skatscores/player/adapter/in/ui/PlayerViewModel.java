package com.damhoe.skatscores.player.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.skatscores.player.application.ports.in.UpdatePlayerUseCase;
import com.damhoe.skatscores.player.domain.Player;

import java.util.List;

public class PlayerViewModel extends ViewModel {

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
        return getPlayerUseCase.deletePlayer(player.getId());
    }

    public boolean isExistentName(String name) {
        return getPlayerUseCase.isNameExistent(name);
    }

    public Result<Player> updatePlayer(Player player) {
        selectedPlayer.postValue(player);
        return updatePlayerUseCase.renamePlayer(player.getId(), player.getName());
    }
}