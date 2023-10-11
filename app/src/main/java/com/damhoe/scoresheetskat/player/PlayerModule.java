package com.damhoe.scoresheetskat.player;

import com.damhoe.scoresheetskat.player.adapter.out.PlayerRepository;
import com.damhoe.scoresheetskat.player.application.PlayerService;
import com.damhoe.scoresheetskat.player.application.ports.in.GetPlayersUseCase;
import com.damhoe.scoresheetskat.player.application.ports.in.ManagePlayerUseCase;
import com.damhoe.scoresheetskat.player.application.ports.out.CreatePlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.LoadPlayerPort;
import com.damhoe.scoresheetskat.player.application.ports.out.UpdatePlayerPort;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class PlayerModule {
   @Binds
   abstract ManagePlayerUseCase bindManagePlayerUseCase(PlayerService playerService);
   @Binds
   abstract GetPlayersUseCase bindGetPlayersUseCase(PlayerService playerService);
   @Binds
   abstract CreatePlayerPort bindCreatePlayerPort(PlayerRepository playerRepository);
   @Binds
   abstract LoadPlayerPort bindLoadPlayerPort(PlayerRepository playerRepository);
   @Binds
   abstract UpdatePlayerPort bindUpdatePlayerPort(PlayerRepository playerRepository);
}
