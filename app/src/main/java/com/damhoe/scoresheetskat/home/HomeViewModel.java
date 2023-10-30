package com.damhoe.scoresheetskat.home;

import android.graphics.Paint;
import android.util.Pair;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.R;
import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.domain.GamePreview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private final LoadGameUseCase loadGameUseCase;
    private final MutableLiveData<List<GamePreview>> openGames = new MutableLiveData<>();

    // First number gives the total number of games
    // Second number gives the number of open games
    private final MutableLiveData<Pair<Integer, Integer>> numberOfGames = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> numberOfGamesYear = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> numberOfGamesMonth = new MutableLiveData<>();

    @Inject
    public HomeViewModel(LoadGameUseCase loadGameUseCase) {
        this.loadGameUseCase = loadGameUseCase;
        updateData();
    }

    LiveData<Pair<Integer, Integer>> getNumberOfGames() {
        return numberOfGames;
    }

    LiveData<Pair<Integer, Integer>> getNumberOfGamesYear() {
        return numberOfGamesYear;
    }

    LiveData<Pair<Integer, Integer>> getNumberOfGamesMonth() {
        return numberOfGamesMonth;
    }

    LiveData<List<GamePreview>> getOpenGames() {
        return openGames;
    }

    public void updateData() {
        // Reload data from repositories
        numberOfGames.setValue(new Pair<>(10, 2));
        numberOfGamesMonth.setValue(new Pair<>(3, 2));
        numberOfGamesYear.setValue(new Pair<>(8, 2));
        List<GamePreview> previews = new ArrayList<>();
        GamePreview p = new GamePreview();
        p.setDate(Calendar.getInstance().getTime());
        p.setGameId(1234l);
        p.setFinished(false);
        p.setTitle("Skatrunde 001");
        List<String> names = new ArrayList<>();
        names.add("Hans");
        names.add("Marie H.");
        names.add("Nele");
        p.setPlayerNames(names);
        previews.add(p);
        previews.add(p);
        previews.add(p);
        openGames.setValue(previews);
    }
}