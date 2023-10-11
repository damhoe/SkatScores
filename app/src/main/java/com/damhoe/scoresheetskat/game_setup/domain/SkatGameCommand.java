package com.damhoe.scoresheetskat.game_setup.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.damhoe.scoresheetskat.game.domain.SkatSettings;

import java.util.Objects;

public class SkatGameCommand extends GameCommand<SkatSettings> {
    public SkatGameCommand(String title, int numberOfPlayers, SkatSettings settings) {
        this.settings = settings;
        this.title = title;
        this.numberOfPlayers = numberOfPlayers;
    }

    @Override
    public boolean isValid() {
        return !Objects.equals(title, "");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flag) {
        out.writeString(title);
        out.writeInt(numberOfPlayers);
        out.writeParcelable(settings, flag);
    }

    public static final Parcelable.Creator<SkatGameCommand> CREATOR = new Creator<SkatGameCommand>() {
        @Override
        public SkatGameCommand createFromParcel(Parcel in) {
            return new SkatGameCommand(in);
        }

        @Override
        public SkatGameCommand[] newArray(int size) {
            return new SkatGameCommand[size];
        }
    };

    private SkatGameCommand(Parcel in) {
        title = in.readString();
        numberOfPlayers = in.readInt();
        settings = in.readParcelable(SkatSettings.class.getClassLoader());
    }
}
