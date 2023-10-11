package com.damhoe.scoresheetskat.score.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.score.domain.ScoreEvent;
import com.damhoe.scoresheetskat.score.domain.SkatScore;

public class SharedScoreResponseViewModel extends ViewModel {
   private final MutableLiveData<ScoreEvent> scoreEvent = new MutableLiveData<>();

   public void setScoreEvent(ScoreEvent scoreEvent) {
      this.scoreEvent.postValue(scoreEvent);
   }

   public LiveData<ScoreEvent> getScoreEvent() {
      return scoreEvent;
   }
}
