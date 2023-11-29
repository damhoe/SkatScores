package com.damhoe.skatscores.player.adapter.in.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.skatscores.player.domain.ProgressInfo;

public class PlayerStatisticsViewModel extends ViewModel {

    private final MutableLiveData<ProgressInfo> overallProgress = new MutableLiveData<>();
    private final MutableLiveData<ProgressInfo> thisYearProgress = new MutableLiveData<>();
    private final MutableLiveData<ProgressInfo> thisMonthProgress = new MutableLiveData<>();

    public LiveData<ProgressInfo> getOverallProgress() {
        return overallProgress;
    }

    public LiveData<ProgressInfo> getThisYearProgress() {
        return thisYearProgress;
    }

    public LiveData<ProgressInfo> getThisMonthProgress() {
        return thisMonthProgress;
    }

    public PlayerStatisticsViewModel() {
        // Reload data from repositories
        thisMonthProgress.setValue(new ProgressInfo(2, 2));
        thisYearProgress.setValue(new ProgressInfo(11, 4));
        overallProgress.setValue(new ProgressInfo(12, 4));
    }
}