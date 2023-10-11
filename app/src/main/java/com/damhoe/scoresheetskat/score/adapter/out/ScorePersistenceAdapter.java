package com.damhoe.scoresheetskat.score.adapter.out;

import com.damhoe.scoresheetskat.score.domain.SkatScore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScorePersistenceAdapter {
    private final List<SkatScore> scores;

    @Inject
    public ScorePersistenceAdapter() {
        scores = new ArrayList<>();
    }

    public long insertScore(SkatScore score) {
        score.setId(UUID.randomUUID().getMostSignificantBits());
        scores.add(score);
        return score.getId();
    }

    public boolean updateScore(SkatScore score) {
        for (int i = 0; i < scores.size(); i++) {
            if (scores.get(i).getId() == score.getId()) {
                scores.set(i, score);
                return true;
            }
        }
        return false;
    }

    public List<SkatScore> loadScores(long gameId) {
        assert gameId != -1;
        return scores;
    }

    public SkatScore getScore(long id) {
        for (SkatScore score : scores) {
            if (score.getId() == id) {
                return score;
            }
        }
        return null;
    }

    public SkatScore deleteScore(long id) {
        for (int j = 0; j < scores.size(); j++) {
            if (scores.get(j).getId() == id) {
                return scores.remove(j);
            }
        }
        return null;
    }
}
