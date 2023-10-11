package com.damhoe.scoresheetskat.player.application;

import android.content.res.Resources;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.base.SearchResult;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.LoadPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;
import com.damhoe.scoresheetskat.player.domain.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class PlayerService implements ManagePlayerUseCase, GetPlayersUseCase {

    private final LoadPlayerPort loadPlayerPort;
    private final CreatePlayerPort createPlayerPort;
    private final UpdatePlayerPort updatePlayerPort;

    @Inject
    PlayerService(LoadPlayerPort loadPlayerPort,
                  CreatePlayerPort createPlayerPort,
                  UpdatePlayerPort updatePlayerPort) {
        this.loadPlayerPort = loadPlayerPort;
        this.createPlayerPort = createPlayerPort;
        this.updatePlayerPort = updatePlayerPort;
    }

    @Override
    public Result<Player> createPlayer(String name) {
        if (createPlayerPort.isPlayerNameExistent(name)) {
            return Result.failure("A player with this name already exists.");
        }
        Player p = createPlayerPort.createPlayer(name);
        return Result.success(p);
    }

    @Override
    public Result<Boolean> renamePlayer(long id, String name) {
        if (isEmptyName(name))
            return Result.failure("Invalid player name");
        if (createPlayerPort.isPlayerNameExistent(name)) {
            return Result.failure(
                    Resources.getSystem().getString(R.string.error_player_name_exists));
        }
        return Result.success(updatePlayerPort.updateName(id, name));
    }

    @Override
    public Result<Player> deletePlayer(Player player) {
        return Result.success(createPlayerPort.deletePlayer(player.getID()));
    }

    @Override
    public Result<Boolean> checkIfNameExists(String name) {
        try {
            return Result.success(createPlayerPort.isPlayerNameExistent(name));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    private boolean isEmptyName(String name) {
        return Objects.equals(name, "");
    }

    @Override
    public Player getPlayer(long id) {
        return loadPlayerPort.loadPlayer(id);
    }

    @Override
    public SearchResult<Player> searchForPlayersWithName(String name) {
        if (isEmptyName(name)) {
            return new SearchResult.Builder<Player>()
                    .hits(Collections.emptyList())
                    .build();
        }
        return loadPlayerPort.searchByName(name);
    }

    @Override
    public List<Player> loadAll() {
        return loadPlayerPort.loadAllPlayers();
    }
}
