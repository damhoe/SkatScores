package com.damhoe.scoresheetskat.score.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.application.ports.in.CreateScoreUseCase;
import com.damhoe.scoresheetskat.score.domain.ScoreRequest;
import com.damhoe.scoresheetskat.score.domain.SkatResult;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;
import com.damhoe.scoresheetskat.score.domain.SkatSuit;

public class ScoreViewModel extends ViewModel {
    private final CreateScoreUseCase createScoreUseCase;

    private final String[] playerNames;
    private final int[] playerPositions;
    private final long scoreToUpdateId; // optional

    private final MutableLiveData<SkatScoreCommand> scoreCommand = new MutableLiveData<>();

    LiveData<Boolean> isResultsEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isResultsEnabled());
    LiveData<Boolean> isSuitsEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isSuitsEnabled());
    LiveData<Boolean> isSpitzenEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isSpitzenEnabled());
    LiveData<Boolean> isIncreaseSpitzenEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isIncreaseSpitzenEnabled());
    LiveData<Boolean> isDecreaseSpitzenEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isDecreaseSpitzenEnabled());
    LiveData<Boolean> isOuvertEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isOuvertEnabled());
    LiveData<Boolean> isSchneiderSchwarzEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isSchneiderSchwarzEnabled());
    LiveData<Boolean> isHandEnabled = Transformations.map(scoreCommand,
            cmd -> ScoreUIElementsStateManager.fromCommand(cmd).isHandEnabled());
    LiveData<SkatResult> getSkatResult =
            Transformations.map(scoreCommand, SkatScoreCommand::getResult);
    LiveData<SkatSuit> getSuit = Transformations.map(scoreCommand, SkatScoreCommand::getSuit);

    public ScoreViewModel(Builder builder) {
        createScoreUseCase = builder.createScoreUseCase;
        playerNames = builder.playerNames;
        playerPositions = builder.playerPositions;
        scoreCommand.setValue(builder.command);
        scoreToUpdateId = builder.scoreToUpdateId;
    }

    public LiveData<SkatScoreCommand> getScoreCommand() {
        return scoreCommand;
    }

    public SkatScore createScore() {
        Result<SkatScore> scoreResult = createScoreUseCase.createScore(scoreCommand.getValue());
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
        Result<SkatScore> scoreResult = createScoreUseCase
                .updateScore(scoreToUpdateId, scoreCommand.getValue());
        if (scoreResult.isFailure()) {
            return null;
        }
        return scoreResult.getValue();
    }

    public void setHand(boolean isHand) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setHand(isHand);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setSchneider(boolean isSchneider) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setSchneider(isSchneider);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setSchneiderAnnounced(boolean isSchneiderAnnounced) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setSchneiderAnnounced(isSchneiderAnnounced);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setSchwarz(boolean isSchwarz) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setSchwarz(isSchwarz);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setSchwarzAnnounced(boolean isSchwarzAnnounced) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setSchwarzAnnounced(isSchwarzAnnounced);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setOuvert(boolean isOuvert) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setOuvert(isOuvert);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setResult(SkatResult result) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            if (skatScoreCommand.getResult() == SkatResult.OVERBID) {
                skatScoreCommand.setSuit(SkatSuit.CLUBS);
                skatScoreCommand.setSpitzen(1);
            }
            if (result == SkatResult.OVERBID) {
                skatScoreCommand.setSuit(SkatSuit.INVALID);
                skatScoreCommand.resetSpitzen();
                skatScoreCommand.resetWinLevels();
            }
            skatScoreCommand.setResult(result);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setSpitzen(int spitzen) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setSpitzen(spitzen);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setSuit(SkatSuit suit) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            skatScoreCommand.setSuit(suit);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setPlayerPosition(int position) {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            if (skatScoreCommand.getResult() == SkatResult.PASSE) {
                skatScoreCommand.setResult(SkatResult.WON);
                skatScoreCommand.setSuit(SkatSuit.CLUBS);
                skatScoreCommand.setSpitzen(1);
            }
            skatScoreCommand.setPlayerPosition(position);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public void setPasse() {
        SkatScoreCommand skatScoreCommand = scoreCommand.getValue();
        if (skatScoreCommand != null) {
            // If passed set player position to invalid value
            skatScoreCommand.setSuit(SkatSuit.INVALID);
            skatScoreCommand.resetWinLevels();
            skatScoreCommand.resetSpitzen();
            skatScoreCommand.setPlayerPosition(-1);
            skatScoreCommand.setResult(SkatResult.PASSE);
            scoreCommand.postValue(skatScoreCommand);
        }
    }

    public static class Builder {
        private final CreateScoreUseCase createScoreUseCase;
        private SkatScoreCommand command = new SkatScoreCommand();
        private String[] playerNames;
        private int[] playerPositions;
        private long scoreToUpdateId = -1L;

        public Builder(CreateScoreUseCase createScoreUseCase) {
            this.createScoreUseCase = createScoreUseCase;
        }

        public Builder setRequest(ScoreRequest request) {
            command.setGameId(request.getGameId());
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