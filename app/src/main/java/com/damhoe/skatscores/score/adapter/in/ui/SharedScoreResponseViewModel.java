package com.damhoe.skatscores.score.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.score.domain.ScoreResult;

public class SharedScoreResponseViewModel extends ViewModel {
   private MutableLiveData<ScoreResult> scoreResult = new MutableLiveData<>();

   /**
    * Called after creation of the viewModel with activity scope
    * to reset the stored data.
    */
   public void reset() {
      scoreResult = new MutableLiveData<>();
   }

   public void setScoreResult(ScoreResult scoreResult) {
      this.scoreResult.postValue(scoreResult);
   }

   public LiveData<ScoreResult> getScoreResult() {
      return scoreResult;
   }
}
