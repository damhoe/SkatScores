package com.damhoe.skatscores.player;

import com.damhoe.skatscores.player.adapter.out.PlayerRepository;
import com.damhoe.skatscores.player.adapter.out.PlayerStatisticsRepository;
import com.damhoe.skatscores.player.application.PlayerService;
import com.damhoe.skatscores.player.application.PlayerStatisticsService;
import com.damhoe.skatscores.player.application.ports.in.GetPlayerStatisticsUseCase;
import com.damhoe.skatscores.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.skatscores.player.application.ports.in.UpdatePlayerUseCase;
import com.damhoe.skatscores.player.application.ports.out.CreatePlayerPort;
import com.damhoe.skatscores.player.application.ports.out.GetPlayerPort;
import com.damhoe.skatscores.player.application.ports.out.GetPlayerStatisticsPort;
import com.damhoe.skatscores.player.application.ports.out.UpdatePlayerPort;

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
   @Binds
   abstract GetPlayerStatisticsUseCase bindPlayerStatisticsUseCase(PlayerStatisticsService service);
   @Binds
   abstract GetPlayerStatisticsPort bindPlayerStatisticsPort(PlayerStatisticsRepository repository);
}
