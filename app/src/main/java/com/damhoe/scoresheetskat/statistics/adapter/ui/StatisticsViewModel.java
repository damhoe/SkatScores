package com.damhoe.scoresheetskat.statistics.adapter.ui;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatisticsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    // First number gives the total number of games
    // Second number gives the number of open games
    private final MutableLiveData<Pair<Integer, Integer>> numberOfGames = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> numberOfGamesYear = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> numberOfGamesMonth = new MutableLiveData<>();

    public StatisticsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");

        // Reload data from repositories
        numberOfGames.setValue(new Pair<>(10, 2));
        numberOfGamesMonth.setValue(new Pair<>(3, 2));
        numberOfGamesYear.setValue(new Pair<>(8, 2));
    }

    public LiveData<String> getText() {
        return mText;
    }
}