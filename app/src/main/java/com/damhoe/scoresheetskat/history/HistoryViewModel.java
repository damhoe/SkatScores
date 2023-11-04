package com.damhoe.scoresheetskat.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.damhoe.scoresheetskat.game.application.ports.in.LoadGameUseCase;
import com.damhoe.scoresheetskat.game.domain.GamePreview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class HistoryViewModel extends ViewModel {

    private final LoadGameUseCase loadGameUseCase;

    private final MutableLiveData<List<GamePreview>> lastMonthGames = new MutableLiveData<>();
    private final MutableLiveData<List<GamePreview>> oldGames = new MutableLiveData<>();

    @Inject
    public HistoryViewModel(LoadGameUseCase loadGameUseCase) {
        this.loadGameUseCase = loadGameUseCase;
        initData();
    }

    LiveData<List<GamePreview>> getOldGames() {
        return oldGames;
    }

    LiveData<List<GamePreview>> getLastMonthGames() {
        return lastMonthGames;
    }

    public void initData() {
        List<GamePreview> previews = new ArrayList<>();
        GamePreview p = new GamePreview();
        p.setDate(Calendar.getInstance().getTime());
        p.setGameId(1234L);
        p.setFinished(false);
        p.setTitle("My game 1");
        List<String> names = new ArrayList<>();
        names.add("Hans");
        names.add("Marie H.");
        names.add("Nele");
        p.setPlayerNames(names);
        previews.add(p);
        previews.add(p);
        previews.add(p);
        lastMonthGames.setValue(previews);
        oldGames.setValue(previews);
    }
}