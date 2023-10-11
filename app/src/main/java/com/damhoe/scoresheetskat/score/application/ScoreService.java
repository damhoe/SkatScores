package com.damhoe.scoresheetskat.score.application;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.score.application.ports.in.CUDScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.in.LoadScoreUseCase;
import com.damhoe.scoresheetskat.score.application.ports.out.GetScoresPort;
import com.damhoe.scoresheetskat.score.application.ports.out.CUDScorePort;
import com.damhoe.scoresheetskat.score.domain.SkatScore;
import com.damhoe.scoresheetskat.score.domain.SkatScoreCommand;

import java.util.List;

import javax.inject.Inject;

public class ScoreService implements CUDScoreUseCase, LoadScoreUseCase {

    private final GetScoresPort getScoresPort;
    private final CUDScorePort cudScorePort;

    @Inject
    ScoreService(GetScoresPort getScoresPort, CUDScorePort cudScorePort) {
        this.getScoresPort = getScoresPort;
        this.cudScorePort = cudScorePort;
    }

    @Override
    public Result<SkatScore> createScore(SkatScoreCommand command) {
        SkatScore score = new SkatScore(command);
        score = cudScorePort.saveScore(score);
        if (score != null) {
            return Result.success(score);
        }
        return Result.failure("Unable to create Skat score.");
    }

    @Override
    public List<SkatScore> loadScores(long gameId) {
        return getScoresPort.loadScores(gameId);
    }

    @Override
    public Result<SkatScore> loadScore(long id) {
        SkatScore score = getScoresPort.getScore(id);
        if (score == null) {
            return Result.failure("Unable to load score with id: " + id);
        }
        return Result.success(score);
    }

    @Override
    public Result<SkatScore> updateScore(long id, SkatScoreCommand command) {
        SkatScore score = new SkatScore(command);
        score.setId(id);
        boolean succeeded = cudScorePort.updateScore(score);
        if (!succeeded) {
            return Result.failure("Unable to update Skat score with id: " + id);
        }
        return Result.success(score);
    }

    @Override
    public Result<SkatScore> deleteScore(long id) {
        SkatScore score = cudScorePort.deleteScore(id);
        if (score == null) {
            return Result.failure("Unable to delete score with id: " + id);
        }
        return Result.success(score);
    }
}
