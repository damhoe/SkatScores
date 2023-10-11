package com.damhoe.scoresheetskat.player.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.base.SearchResult;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;

import kotlin.contracts.ReturnsNotNull;

public class PlayersViewModel extends ViewModel {

    private final ManagePlayerUseCase managePlayerUseCase;
    private final GetPlayersUseCase getPlayersUseCase;

    private MutableLiveData<List<Player>> players = new MutableLiveData<>();

    private MutableLiveData<Player> selectedPlayer = new MutableLiveData<>();

    public PlayersViewModel(ManagePlayerUseCase managePlayerUseCase, GetPlayersUseCase getPlayersUseCase) {
        this.managePlayerUseCase = managePlayerUseCase;
        this.getPlayersUseCase = getPlayersUseCase;
        players.setValue(getPlayersUseCase.loadAll());
    }

    public void initialize() {
        players.postValue(getPlayersUseCase.loadAll());
    }

    public LiveData<List<Player>> getPlayers() {
        return players;
    }

    LiveData<Player> getSelectedPlayer() {
        return selectedPlayer;
    }

    public void selectPlayer(Player player) {
        selectedPlayer.postValue(player);
    }

    public Player addPlayer(String name) {
        Result<Player> result = managePlayerUseCase.createPlayer(name);
        List<Player> playerList = players.getValue();
        if (result.isFailure()) {
            return null;
        }
        Player newPlayer = result.getValue();
        if (playerList != null) {
            playerList.add(newPlayer);
            players.postValue(playerList);
        }
        return newPlayer;
    }

    public Player removePlayer(Player player) {
        Result<Player> result = managePlayerUseCase.deletePlayer(player);
        if (result.isFailure()) {
            throw new RuntimeException(result.getMessage());
        }
        Player removedPlayer = result.getValue();
        List<Player> playerList = players.getValue();
        if (playerList != null) {
            playerList.removeIf(p -> p.getID() == player.getID());
            players.postValue(playerList);
        }
        return removedPlayer;
    }

    public Player findPlayerByName(String name) {
        SearchResult<Player> searchResult = getPlayersUseCase.searchForPlayersWithName(name);
        if (searchResult.numberOfHits == 1) {
            return searchResult.hits.get(0);
        }
        return null;
    }

    public boolean isExistentName(String name) {
        Result<Boolean> result = managePlayerUseCase.checkIfNameExists(name);
        if (result.isFailure()) {
            throw new RuntimeException("Failed to check for player names.");
        }
        return result.getValue();
    }

    public void updatePlayer(Player player) {
        managePlayerUseCase.renamePlayer(player.getID(), player.getName());
    }
}