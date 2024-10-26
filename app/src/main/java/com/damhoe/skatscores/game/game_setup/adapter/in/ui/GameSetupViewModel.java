package com.damhoe.skatscores.game.game_setup.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.game.game_home.Constants;
import com.damhoe.skatscores.game.game_home.domain.SkatSettings;
import com.damhoe.skatscores.game.game_setup.domain.SkatGameCommand;

public class GameSetupViewModel extends ViewModel {
    private final MutableLiveData<String> title = new MutableLiveData<>();
    private final MutableLiveData<Integer> numberOfPlayers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTournamentScoring = new MutableLiveData<>();
    private final MutableLiveData<Integer> numberOfRounds = new MutableLiveData<>();
    private final MediatorLiveData<SkatGameCommand> skatGameCommand = new MediatorLiveData<>();
    public LiveData<Boolean> isValidCommand =
            Transformations.map(skatGameCommand, SkatGameCommand::isValid);

    public GameSetupViewModel() {
        skatGameCommand.addSource(title, s -> updateSkatGameCommand());
        skatGameCommand.addSource(numberOfPlayers, x -> updateSkatGameCommand());
        skatGameCommand.addSource(isTournamentScoring, b -> updateSkatGameCommand());
        skatGameCommand.addSource(numberOfRounds, x -> updateSkatGameCommand());
        numberOfPlayers.setValue(Constants.DEFAULT_NUMBER_OF_PLAYERS);
        isTournamentScoring.setValue(false);
        numberOfRounds.setValue(Constants.DEFAULT_NUMBER_OF_ROUNDS);
        title.setValue("");
    }

    /** @noinspection DataFlowIssue*/
    private void updateSkatGameCommand() {
        // Create a new SkatGameCommand based on the current values of sources
        SkatGameCommand newCommand = new SkatGameCommand(
                title.getValue(),
                numberOfPlayers.getValue(),
                new SkatSettings(isTournamentScoring.getValue(), numberOfRounds.getValue())
        );

        // Set the new value for skatGameCommand
        skatGameCommand.setValue(newCommand);
    }

    public LiveData<String> getTitle() {
        return title;
    }

    public void updateTitle(String newTitle) {
        title.postValue(newTitle);
    }

    public void setTitle(String newTitle) {
        title.setValue(newTitle);
    }

    public LiveData<Integer> getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public LiveData<Boolean> isTournamentScoring() {
        return isTournamentScoring;
    }

    public void updateScoring(boolean newIsTournamentScoring) {
        isTournamentScoring.postValue(newIsTournamentScoring);
    }

    public LiveData<Integer> getNumberOfRounds() {
        return numberOfRounds;
    }

    public void updateNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds.postValue(numberOfRounds);
    }

    public LiveData<SkatGameCommand> getSkatGameCommand() {
        return skatGameCommand;
    }
}