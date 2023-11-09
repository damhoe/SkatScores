package com.damhoe.scoresheetskat.player;

import com.damhoe.scoresheetskat.player.adapter.out.PlayerRepository;
import com.damhoe.scoresheetskat.player.application.PlayerService;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.UpdatePlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.GetPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class PlayerModule {
   @Binds
   abstract UpdatePlayerUseCase bindManagePlayerUseCase(PlayerService playerService);
   @Binds
   abstract GetPlayerUseCase bindGetPlayersUseCase(PlayerService playerService);
   @Binds
   abstract CreatePlayerPort bindCreatePlayerPort(PlayerRepository playerRepository);
   @Binds
   abstract GetPlayerPort bindLoadPlayerPort(PlayerRepository playerRepository);
   @Binds
   abstract UpdatePlayerPort bindUpdatePlayerPort(PlayerRepository playerRepository);
}
