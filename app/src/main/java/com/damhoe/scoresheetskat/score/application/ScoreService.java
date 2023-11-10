package com.damhoe.scoresheetskat.score.application;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.application.ports.in.CreateScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.in.GetScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.out.CreateScorePort;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;

import java.util.List;

import javax.inject.Inject;

public class ScoreService implements CreateScoreUseCase, GetScoreUseCase {

    private final GetScoresPort getScoresPort;
    private final CreateScorePort createScorePort;

    @Inject
    ScoreService(GetScoresPort getScoresPort, CreateScorePort createScorePort) {
        this.getScoresPort = getScoresPort;
        this.createScorePort = createScorePort;
    }

    @Override
    public Result<SkatScore> createScore(SkatScoreCommand command) {
        SkatScore score = new SkatScore(command);
        score = createScorePort.saveScore(score);
        return Result.success(score);
    }

    @Override
    public List<SkatScore> getScores(long gameId) {
        return getScoresPort.getScores(gameId);
    }

    @Override
    public Result<SkatScore> getScore(long id) {
        return Result.success(getScoresPort.getScore(id));
    }

    @Override
    public Result<SkatScore> updateScore(long id, SkatScoreCommand command) {
        SkatScore score = new SkatScore(command);
        score.setId(id);
        return Result.success(createScorePort.updateScore(score));
    }

    @Override
    public Result<SkatScore> deleteScore(long id) {
        return Result.success(createScorePort.deleteScore(id));
    }

    @Override
    public void deleteScoresForGame(long gameId) {
        createScorePort.deleteScoresForGame(gameId);
    }
}
