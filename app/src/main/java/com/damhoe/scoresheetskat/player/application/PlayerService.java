package com.damhoe.scoresheetskat.player.application;

import android.database.SQLException;

import androidx.lifecycle.LiveData;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.player.application.ports.in.UpdatePlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.GetPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

public class PlayerService implements UpdatePlayerUseCase, GetPlayerUseCase {

    private final GetPlayerPort mGetPlayerPort;
    private final CreatePlayerPort mCreatePlayerPort;
    private final UpdatePlayerPort mUpdatePlayerPort;

    @Inject
    PlayerService(
            GetPlayerPort getPlayerPort,
            CreatePlayerPort createPlayerPort,
            UpdatePlayerPort updatePlayerPort
    ) {
        mGetPlayerPort = getPlayerPort;
        mCreatePlayerPort = createPlayerPort;
        mUpdatePlayerPort = updatePlayerPort;
    }

    @Override
    public LiveData<List<Player>> getPlayersLiveData() {
        return mGetPlayerPort.getPlayersLiveData();
    }

    @Override
    public List<Player> getPlayers() {
        return mGetPlayerPort.getPlayers();
    }

    @Override
    public Result<Player> getPlayer(long id) {
        try {
            Player player = mGetPlayerPort.getPlayer(id);
            if (player == null) {
                return Result.failure("Unable to find player with id: " + id);
            }
            return Result.success(player);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Result.failure(ex.getMessage());
        }
    }

    @Override
    public Result<Player> findOrCreate(String name) {
        try {
            if (Player.isDummyName(name)) {
                return Result.success(Player.createDummy(name));
            }

            List<Player> players = mGetPlayerPort.getPlayers();
            Optional<Player> optionalPlayer = players.stream()
                    .filter(x -> x.getName().equals(name))
                    .findFirst();

            return optionalPlayer.map(Result::success)
                    .orElseGet(() -> createPlayer(name));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Result.failure(ex.getMessage());
        }
    }

    @Override
    public Result<Player> createPlayer(String name) {
        try {
            return Result.success(mCreatePlayerPort.createPlayer(name));
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Result.failure(ex.getMessage());
        }
    }

    @Override
    public Result<Player> deletePlayer(long id) {
        try {
            return Result.success(mCreatePlayerPort.deletePlayer(id));
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Result.failure(ex.getMessage());
        }
    }

    @Override
    public boolean isNameExistent(String name) {
        List<Player> players = getPlayersLiveData().getValue();
        if (players == null) {
            return false;
        }
        return players.stream().anyMatch(x -> x.getName().equals(name));
    }

    @Override
    public void refreshPlayers() {
        mGetPlayerPort.refreshPlayersFromRepository();
    }

    @Override
    public Result<Player> renamePlayer(long id, String name) {
        try {
            return Result.success(mUpdatePlayerPort.updateName(id, name));
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Result.failure(ex.getMessage());
        }
    }
}
