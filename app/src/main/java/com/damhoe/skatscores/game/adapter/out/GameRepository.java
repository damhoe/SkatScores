package com.damhoe.skatscores.game.adapter.out;

import android.content.res.Resources;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.damhoe.skatscores.KotlinResultWrapper;
import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.adapter.out.models.PlayerMatchDTO;
import com.damhoe.skatscores.game.adapter.out.models.SkatGameDTO;
import com.damhoe.skatscores.game.adapter.out.models.SkatSettingsDTO;
import com.damhoe.skatscores.game.application.ports.out.GamePort;
import com.damhoe.skatscores.game.domain.Game;
import com.damhoe.skatscores.game.domain.SkatGame;
import com.damhoe.skatscores.game.domain.SkatGamePreview;
import com.damhoe.skatscores.game.domain.SkatSettings;
import com.damhoe.skatscores.player.application.ports.in.GetPlayerUseCase;
import com.damhoe.skatscores.player.domain.Player;
import com.damhoe.skatscores.score.application.ports.in.CreateScoreUseCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GameRepository implements GamePort {
   private final GetPlayerUseCase mGetPlayerUseCase;
   private final CreateScoreUseCase mCreateScoreUseCase;
   private final GamePersistenceAdapter mGameAdapter;
   private final PlayerMatchPersistenceAdapter mPlayerMatchAdapter;
   private final SettingsPersistenceAdapter mSettingsPersistenceAdapter;
   private final GameMapper mGameMapper;

   private final MutableLiveData<List<SkatGamePreview>> skatGamePreviews = new MutableLiveData<>();

   @Inject
   public GameRepository(
           GetPlayerUseCase getPlayerUseCase,
           CreateScoreUseCase createScoreUseCase,
           GamePersistenceAdapter gameAdapter,
           PlayerMatchPersistenceAdapter playerMatchAdapter,
           SettingsPersistenceAdapter settingsPersistenceAdapter,
           GameMapper gameMapper
   ) {
      mGetPlayerUseCase = getPlayerUseCase;
      this.mCreateScoreUseCase = createScoreUseCase;
      mGameAdapter = gameAdapter;
      mPlayerMatchAdapter = playerMatchAdapter;
      mSettingsPersistenceAdapter = settingsPersistenceAdapter;
      mGameMapper = gameMapper;

      // Load data
      refreshGamePreviews();
   }

   @Override
   public SkatGame saveSkatGame(SkatGame game) {
      // Save settings
      SkatSettingsDTO settingsDTO =
              SettingsMapper.mapSkatSettingsToSkatSettingsDTO(game.getSettings());
      long settingsId = mSettingsPersistenceAdapter.insertSettings(settingsDTO);

      // Insert game into database
      SkatGameDTO dto = GameMapper.mapSkatGameToSkatGameDTO(game, settingsId);
      long gameId = mGameAdapter.insertGame(dto);
      game.setId(gameId);

      // Save players
      for (int k = 0; k < game.getPlayers().size(); k++) {
         long playerId = game.getPlayers().get(k).getId();

         if (mPlayerMatchAdapter.isValidPlayerId(playerId)) {
            PlayerMatchDTO playerMatch =
                    new PlayerMatchDTO(gameId, playerId, k);
            mPlayerMatchAdapter.insertPlayerMatch(playerMatch);
         }
      }

      // Update live data
      refreshGamePreviews();

      return game;
   }

   @Override
   public SkatGame deleteGame(long id) {
      SkatGame skatGame = getGame(id);

      // Delete settings
      mSettingsPersistenceAdapter.deleteSettings(skatGame.getSettings().getId());
      // Delete player matches
      mPlayerMatchAdapter.deletePlayerMatchesForGame(skatGame.getId());
      // Delete scores
      KotlinResultWrapper.Companion.deleteScoresForGame(mCreateScoreUseCase, skatGame.getId());
      // Delete game
      mGameAdapter.deleteGame(skatGame.getId());

      // Update live data
      List<SkatGamePreview> _previews = skatGamePreviews.getValue();
      if (_previews != null) {
         _previews.removeIf(p -> p.getGameId() == id);
         skatGamePreviews.postValue(_previews);
      }

      return skatGame;

   }

   @Override
   public SkatGame updateGame(SkatGame game) {
      // Update settings
      SkatSettingsDTO settingsDTO =
              SettingsMapper.mapSkatSettingsToSkatSettingsDTO(game.getSettings());
      int result = mSettingsPersistenceAdapter.updateSettings(settingsDTO);

      // Update game
      SkatGameDTO dto = GameMapper.mapSkatGameToSkatGameDTO(game, game.getSettings().getId());
      mGameAdapter.updateGame(dto);

      // Update players
      // Matches are deleted and created again,
      // because they can also become invalid during the update process
      mPlayerMatchAdapter.deletePlayerMatchesForGame(game.getId());
      for (int k = 0; k < game.getPlayers().size(); k++) {
         long playerId = game.getPlayers().get(k).getId();
         if (mPlayerMatchAdapter.isValidPlayerId(playerId)) {
            PlayerMatchDTO playerMatch =
                    new PlayerMatchDTO(game.getId(), playerId, k);
            mPlayerMatchAdapter.insertPlayerMatch(playerMatch);
         }
      }

      // Update live data
      refreshGamePreviews();

      return game;
   }

   @Override
   public LiveData<List<SkatGamePreview>> getUnfinishedGames() {
      return Transformations.map(skatGamePreviews,
              x -> filterByRunningState(x, Game.RunState.RUNNING)
      );
   }

   @Override
   public LiveData<List<SkatGamePreview>> getGamesSince(Date oldestDate) {
      return Transformations.map(skatGamePreviews, x -> filterByDate(x, oldestDate));
   }

   @Override
   public void refreshGamePreviews() {
      List<SkatGameDTO> gameDtoList = mGameAdapter.getAllGames();

      // Transform the data to game previews
      List<SkatGamePreview> previewList = gameDtoList.stream()
              .map(dto -> mGameMapper.mapToPreview(
                      dto,
                      SettingsMapper.mapSkatSettingsDTOToSkatSettings(
                              mSettingsPersistenceAdapter.getSettings(dto.getId()).value),
                      loadPlayersForGame(dto.getId(), 3)
              ))
              .collect(Collectors.toList());

      skatGamePreviews.postValue(previewList);
   }

   @Override
   public SkatGame getGame(long id) {
      Result<SkatGameDTO> gameResult = mGameAdapter.getGame(id);
      if (gameResult.isFailure()) {
         throw new Resources.NotFoundException(gameResult.message);
      }

      SkatGameDTO skatGameDTO = gameResult.value;

      // Load settings
      Result<SkatSettingsDTO> settingsResult =
              mSettingsPersistenceAdapter.getSettings(skatGameDTO.getSettingsId());
      if (settingsResult.isFailure()) {
         throw new Resources.NotFoundException(settingsResult.message);
      }
      SkatSettings skatSettings =
              SettingsMapper.mapSkatSettingsDTOToSkatSettings(settingsResult.value);

      // Load players
      // If no player is saved for a specific position
      // fill with a default player
      List<Player> players = loadPlayersForGame(skatGameDTO.getId(), 3); // TODO change hardcoded number of players

      // Map data to skat game
      return mGameMapper.mapSkatGameDTOToSkatGame(skatGameDTO, skatSettings, players);
   }

   /**
    * Load players from database
    * If no player is saved for a specific position
    * fill with a default player
    *
    * @param gameId Id of SkatGame
    * @param count Expected number of players
    * @return List of players
    */
   private List<Player> loadPlayersForGame(long gameId, int count) {
      List<PlayerMatchDTO> playerMatches = mPlayerMatchAdapter.loadPlayerMatches(gameId);

      List<Player> players = new ArrayList<>();
      for (int j = 0; j < count; j++) {
         players.add(null);
      }

      // First, set the saved players
      for (PlayerMatchDTO match: playerMatches) {
         int position = match.getPosition();

         // Check for valid position
         if (position >= count || position < 0) {
            throw new RuntimeException(
                    String.format("Player was requested for position %d, " +
                            "but only %d players were expected.", position, count)
            );
         }

         Player player = mGetPlayerUseCase.getPlayer(match.getPlayerId()).value;
         players.set(position, player);
      }

      // Fill empty spots with default players
      for (int j = 0; j < count; j++) {
         if (players.get(j) == null) {
            Player defaultPlayer = new Player("Player " + (j+1));
            players.set(j, defaultPlayer);
         }
      }

      return players;
   }

   private List<SkatGamePreview> filterByDate(List<SkatGamePreview> previews, Date oldestDate) {
      return previews.stream()
              .filter(x -> x.getDate().after(oldestDate))
              .collect(Collectors.toList());
   }

   private List<SkatGamePreview> filterByRunningState(
           List<SkatGamePreview> previews,
           Game.RunState state
   ) {
      return previews.stream()
              .filter(x -> x.getGameRunStateInfo().isFinished() == (state == Game.RunState.FINISHED))
              .collect(Collectors.toList());
   }
}
