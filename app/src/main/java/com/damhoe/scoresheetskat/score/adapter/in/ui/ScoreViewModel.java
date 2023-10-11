package com.damhoe.scoresheetskat.score.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.application.ports.in.CUDScoreUseCase;
import com.damhoe.scoresheetskat.score.domain.ScoreRequest;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;
import com.damhoe.scoresheetskat.score.domain.SkatSuit;

import kotlin.Triple;

public class ScoreViewModel extends ViewModel {
    private final CUDScoreUseCase cudScoreUseCase;
    private long scoreToUpdateId = -1L;

    private final String[] playerNames;
    private final int[] playerPositions;

    private final MutableLiveData<SkatScoreCommand> createSkatScoreCommand =
            new MutableLiveData<>();

    final LiveData<Boolean> isValidCommand =
            Transformations.map(createSkatScoreCommand, SkatScoreCommand::isValid);

    final LiveData<Boolean> isMinSpitzen =
            Transformations.map(createSkatScoreCommand, SkatScoreCommand::isMinSpitzen);

    final LiveData<Boolean> isMaxSpitzen =
            Transformations.map(createSkatScoreCommand, SkatScoreCommand::isMaxSpitzen);

    final LiveData<Boolean> isResultEnabled =
            Transformations.map(createSkatScoreCommand, SkatScoreCommand::isResultEnabled);

    final LiveData<Boolean> isSuitsEnabled = Transformations.map(
            createSkatScoreCommand, SkatScoreCommand::isSuitsEnabled);

    final LiveData<Boolean> isSpitzenEnabled = Transformations.map(
            createSkatScoreCommand, SkatScoreCommand::isSpitzenEnabled);

    final LiveData<Boolean> isOuvertEnabled = Transformations.map(
            createSkatScoreCommand, SkatScoreCommand::isOuvertEnabled);

    final LiveData<Boolean> isSchneiderSchwarzEnabled = Transformations.map(
            createSkatScoreCommand, SkatScoreCommand::isSchneiderSchwarzEnabled);

    final LiveData<Boolean> isHandEnabled = Transformations.map(
            createSkatScoreCommand, SkatScoreCommand::isHandEnabled);

    final LiveData<Boolean> isSchneiderAnnounced = Transformations.map(
            createSkatScoreCommand, SkatScoreCommand::isSchneiderAnnounced);

    final MediatorLiveData<Triple<Boolean, Boolean, Boolean>> spitzenElementsEnabled =
            new MediatorLiveData<>();

    public ScoreViewModel(Builder builder) {
        cudScoreUseCase = builder.cudScoreUseCase;
        playerNames = builder.playerNames;
        playerPositions = builder.playerPositions;
        createSkatScoreCommand.setValue(builder.command);
        scoreToUpdateId = builder.scoreToUpdateId;
        spitzenElementsEnabled.addSource(isSpitzenEnabled, isSpitzenEnabled -> {
            if (isSpitzenEnabled) {
                boolean sliderEnabled = true;
                boolean removeButtonEnabled = Boolean.FALSE.equals(isMinSpitzen.getValue());
                boolean addButtonEnabled = Boolean.FALSE.equals(isMaxSpitzen.getValue());
                spitzenElementsEnabled.postValue(
                        new Triple<>(sliderEnabled, removeButtonEnabled, addButtonEnabled));
            }
            spitzenElementsEnabled.postValue(new Triple<>(false, false, false));
        });
        spitzenElementsEnabled.addSource(isMinSpitzen, isMinSpitzen -> {
            if (Boolean.FALSE.equals(isSpitzenEnabled.getValue())) {
                spitzenElementsEnabled.postValue(new Triple<>(false, false, false));
                return;
            }
            spitzenElementsEnabled.postValue(new Triple<>(
                    true, !isMinSpitzen, Boolean.FALSE.equals(isMaxSpitzen.getValue())));
        });
        spitzenElementsEnabled.addSource(isMaxSpitzen, isMaxSpitzen -> {
            if (Boolean.FALSE.equals(isSpitzenEnabled.getValue())) {
                spitzenElementsEnabled.postValue(new Triple<>(false, false, false));
                return;
            }
            spitzenElementsEnabled.postValue(new Triple<>(
                    true, Boolean.FALSE.equals(isMinSpitzen.getValue()), !isMaxSpitzen));
        });
    }

    public SkatScore createScore() {
        Result<SkatScore> scoreResult = cudScoreUseCase
                .createScore(createSkatScoreCommand.getValue());
        if (scoreResult.isFailure()) {
            return null;
        }
        return scoreResult.getValue();
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public int[] getPlayerPositions() {
        return playerPositions;
    }


    public SkatScore updateScore() {
        Result<SkatScore> scoreResult = cudScoreUseCase
                .updateScore(scoreToUpdateId, createSkatScoreCommand.getValue());
        if (scoreResult.isFailure()) {
            return null;
        }
        return scoreResult.getValue();
    }

    public LiveData<SkatScoreCommand> getSkatScoreCommand() {
        return createSkatScoreCommand;
    }

    public void setHand(boolean isHand) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setHand(isHand);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setSchneider(boolean isSchneider) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setSchneider(isSchneider);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setSchneiderAnnounced(boolean isSchneiderAnnounced) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setSchneiderAnnounced(isSchneiderAnnounced);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setSchwarz(boolean isSchwarz) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setSchwarz(isSchwarz);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setSchwarzAnnounced(boolean isSchwarzAnnounced) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setSchneiderAnnounced(isSchwarzAnnounced);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setOuvert(boolean isOuvert) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setOuvert(isOuvert);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setWon(boolean isWon) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setWon(isWon);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setSpitzen(int spitzen) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setSpitzen(spitzen);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setSuit(SkatSuit suit) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setSuit(suit);
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setPlayerPosition(int position) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setPlayerPosition(position);
            if (position >= 0) {
                command.setPasse(false);
            }
            createSkatScoreCommand.postValue(command);
        }
    }

    public void setPassed(boolean isPassed) {
        SkatScoreCommand command = createSkatScoreCommand.getValue();
        if (command != null) {
            command.setPasse(isPassed);
            // If passed set player position to invalid value
            if (isPassed) {
                command.setPlayerPosition(-1);
            }
            createSkatScoreCommand.postValue(command);
        }
    }

    public static class Builder {
        private final CUDScoreUseCase cudScoreUseCase;
        private SkatScoreCommand command = new SkatScoreCommand();
        private String[] playerNames;
        private int[] playerPositions;
        private long scoreToUpdateId = -1L;

        public Builder(CUDScoreUseCase cudScoreUseCase) {
            this.cudScoreUseCase = cudScoreUseCase;
        }

        public Builder setRequest(ScoreRequest request) {
            playerNames = request.getPlayerNames();
            playerPositions = request.getPlayerPositions();
            return this;
        }

        public Builder fromScore(SkatScore score) {
            command = SkatScoreCommand.fromSkatScore(score);
            scoreToUpdateId = score.getId();
            return this;
        }

        public ScoreViewModel build() {
            return new ScoreViewModel(this);
        }
    }
}