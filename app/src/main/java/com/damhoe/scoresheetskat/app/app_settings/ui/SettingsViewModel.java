package com.damhoe.scoresheetskat.app.app_settings.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.damhoe.scoresheetskat.game.Constants.DEFAULT_NUMBER_OF_PLAYERS;
import static com.damhoe.scoresheetskat.game.Constants.DEFAULT_NUMBER_OF_ROUNDS;

public class SettingsViewModel extends ViewModel {

    public static final String DEFAULT_LANGUAGE = "English";
    private static final boolean DEFAULT_SHOULD_SHOW_UNFINISHED_AS_BADGE = true;
    private static final boolean DEFAULT_IS_TOURNAMENT_SCORING = false;

    private MutableLiveData<String> language;
    public MutableLiveData<String> getLanguage() {
        if (language == null) {
            language = new MutableLiveData<>();
            language.setValue(DEFAULT_LANGUAGE);
        }
        return language;
    }

    public void setLanguage(String language) {
        this.language.postValue(language);
    }

    private MutableLiveData<Boolean> shouldShowUnfinishedAsBadge;
    public MutableLiveData<Boolean> shouldShowUnfinishedAsBadge() {
        if (shouldShowUnfinishedAsBadge == null) {
            shouldShowUnfinishedAsBadge = new MutableLiveData<>();
            shouldShowUnfinishedAsBadge.setValue(DEFAULT_SHOULD_SHOW_UNFINISHED_AS_BADGE);
        }
        return shouldShowUnfinishedAsBadge;
    }
    public void shouldShowUnfinishedAsBadge(boolean shouldShowUnfinishedAsBadge) {
        this.shouldShowUnfinishedAsBadge.postValue(shouldShowUnfinishedAsBadge);
    }

    private MutableLiveData<Integer> numberOfPlayers;
    public MutableLiveData<Integer> getNumberOfPlayers() {
        if (numberOfPlayers == null) {
            numberOfPlayers = new MutableLiveData<>();
            numberOfPlayers.setValue(DEFAULT_NUMBER_OF_PLAYERS);
        }
        return numberOfPlayers;
    }
    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers.postValue(numberOfPlayers);
    }

    private MutableLiveData<Integer> numberOfRounds;
    public MutableLiveData<Integer> getNumberOfRounds() {
        if (numberOfRounds == null) {
            numberOfRounds = new MutableLiveData<>();
            numberOfRounds.setValue(DEFAULT_NUMBER_OF_ROUNDS);
        }
        return numberOfRounds;
    }
    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds.postValue(numberOfRounds);
    }

    private MutableLiveData<Boolean> isTournamentScoring;
    public MutableLiveData<Boolean> isTournamentScoring() {
        if (isTournamentScoring == null) {
            isTournamentScoring = new MutableLiveData<>();
            isTournamentScoring.setValue(DEFAULT_IS_TOURNAMENT_SCORING);
        }
        return isTournamentScoring;
    }
    public void setTournamentScoring(boolean isTournamentScoring) {
        this.isTournamentScoring.postValue(isTournamentScoring);
    }
}