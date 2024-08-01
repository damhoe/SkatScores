package com.damhoe.skatscores.game.adapter.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.persistence.DbHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.damhoe.skatscores.persistence.Utility.getIntFromCursor;
import static com.damhoe.skatscores.persistence.Utility.getLongFromCursor;
import static com.damhoe.skatscores.persistence.Utility.getStringFromCursor;

@Singleton
public class GamePersistenceAdapter {
    private final DbHelper dbHelper;

    @Inject
    public GamePersistenceAdapter(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    protected long insertGame(SkatGameDTO skatGameDTO) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.GAME_COLUMN_TITLE, skatGameDTO.getTitle());
            contentValues.put(DbHelper.GAME_COLUMN_START_DEALER_POSITION, skatGameDTO.getStartDealerPosition());
            contentValues.put(DbHelper.GAME_COLUMN_SETTINGS_ID, skatGameDTO.getSettingsId());
            return db.insert(DbHelper.GAME_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @SuppressLint("Range")
    protected Result<SkatGameDTO> getGame(long id) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + DbHelper.GAME_TABLE_NAME
                            + " WHERE " + DbHelper.GAME_COLUMN_ID + " = ? ",
                    new String[] {String.valueOf(id)});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                SkatGameDTO game = cursorToSkatGameDTO(cursor);
                return Result.success(game);
            } else {
                return Result.failure("SkatGame with id " + id + " does not exist");
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected int updateGame(SkatGameDTO skatGame) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.GAME_COLUMN_TITLE, skatGame.getTitle());
            contentValues.put(DbHelper.GAME_COLUMN_START_DEALER_POSITION, skatGame.getStartDealerPosition());
            contentValues.put(DbHelper.GAME_COLUMN_SETTINGS_ID, skatGame.getSettingsId());
            contentValues.put(DbHelper.GAME_COLUMN_UPDATED_AT, skatGame.getUpdatedAt());
            String whereClause = DbHelper.GAME_COLUMN_ID + " = ? ";
            return db.update(DbHelper.GAME_TABLE_NAME, contentValues, whereClause, new String[] { String.valueOf(skatGame.getId()) });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected Result<SkatGameDTO> deleteGame(long id) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Result<SkatGameDTO> getResult = getGame(id);
            if (getResult.isFailure()) {
                return Result.failure("SkatGame with id " + id + " was not deleted because it did not exist");
            }
            db.execSQL(
                    "DELETE FROM " + DbHelper.GAME_TABLE_NAME
                            + " WHERE " + DbHelper.GAME_COLUMN_ID +  " = ?",
                    new String[] { id + "" });
            SkatGameDTO deletedGame = getResult.value;
            return Result.success(deletedGame);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected List<SkatGameDTO> getAllGames() {
        Cursor cursor = null;
        List<SkatGameDTO> games = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + DbHelper.GAME_TABLE_NAME,
                    null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                SkatGameDTO game = cursorToSkatGameDTO(cursor);
                games.add(game);
                cursor.moveToNext();
            }
            return games;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static SkatGameDTO cursorToSkatGameDTO(Cursor cursor) {
        SkatGameDTO skatGameDTO = new SkatGameDTO();

        long id = getLongFromCursor(cursor, DbHelper.GAME_COLUMN_ID, -1);
        String title = getStringFromCursor(cursor, DbHelper.GAME_COLUMN_TITLE, "");
        String createdAt = getStringFromCursor(cursor, DbHelper.GAME_COLUMN_CREATED_AT, "");
        String updatedAt = getStringFromCursor(cursor, DbHelper.GAME_COLUMN_UPDATED_AT, "");
        int startDealerPosition = getIntFromCursor(cursor, DbHelper.GAME_COLUMN_START_DEALER_POSITION, 0);
        long settingsId = getLongFromCursor(cursor, DbHelper.GAME_COLUMN_SETTINGS_ID, -1);

        skatGameDTO.setId(id);
        skatGameDTO.setTitle(title);
        skatGameDTO.setCreatedAt(createdAt);
        skatGameDTO.setUpdatedAt(updatedAt);
        skatGameDTO.setStartDealerPosition(startDealerPosition);
        skatGameDTO.setSettingsId(settingsId);

        return skatGameDTO;
    }
}
