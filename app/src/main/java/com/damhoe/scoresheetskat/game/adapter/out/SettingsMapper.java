package com.damhoe.scoresheetskat.game.adapter.out;

import com.damhoe.scoresheetskat.game.adapter.out.models.SkatSettingsDTO;
import com.damhoe.scoresheetskat.game.domain.SkatSettings;

public class SettingsMapper {

   public static SkatSettingsDTO mapSkatSettingsToSkatSettingsDTO(SkatSettings settings) {
      SkatSettingsDTO settingsDTO = new SkatSettingsDTO();
      settingsDTO.setNumberOfRounds(settings.getNumberOfRounds());
      settingsDTO.setScoringType(settings.isTournamentScoring() ? 1 : 0);
      settingsDTO.setId(-1);
      return settingsDTO;
   }

   public static SkatSettings mapSkatSettingsDTOToSkatSettings(SkatSettingsDTO settingsDTO) {
      boolean isTournamentScoring = settingsDTO.getScoringType() == 1;
      int numberOfRounds = settingsDTO.getNumberOfRounds();
      SkatSettings settings = new SkatSettings(isTournamentScoring, numberOfRounds);
      settings.setId(settingsDTO.getId());
      return settings;
   }
}
