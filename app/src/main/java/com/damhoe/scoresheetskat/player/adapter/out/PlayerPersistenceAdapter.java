package com.damhoe.scoresheetskat.player.adapter.out;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.game.adapter.out.models.PlayerMatchDTO;
import com.damhoe.scoresheetskat.persistence.DbHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

class PlayerPersistenceAdapter {
    private final DbHelper dbHelper;

    @Inject
    PlayerPersistenceAdapter(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    protected long insertPlayer(PlayerDTO player) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.PLAYER_COLUMN_NAME, player.getName());
            return db.insert(DbHelper.PLAYER_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @SuppressLint("Range")
    protected Result<PlayerDTO> getPlayer(long playerId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM "
                            + DbHelper.PLAYER_TABLE_NAME
                            + " WHERE "
                            + DbHelper.PLAYER_COLUMN_ID + " = ? ",
                    new String[] {String.valueOf(playerId)});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                PlayerDTO player = cursorToPlayerDTO(cursor);
                return Result.success(player);
            } else {
                return Result.failure("Player with id " + playerId + " does not exist");
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

    protected int updatePlayer(PlayerDTO player) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.PLAYER_COLUMN_NAME, player.getName());
            contentValues.put(DbHelper.PLAYER_COLUMN_UPDATED_AT, player.getUpdatedAt());
            String whereClause = DbHelper.PLAYER_COLUMN_ID + " = ? ";
            return db.update(DbHelper.PLAYER_TABLE_NAME, contentValues, whereClause,
                    new String[] { String.valueOf(player.getId()) });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected Result<PlayerDTO> deletePlayer(long id) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Result<PlayerDTO> getResult = getPlayer(id);
            if (getResult.isFailure()) {
                return Result.failure("Player with id " + id + " was not deleted because it did not exist");
            }
            db.execSQL(
                "DELETE FROM " + DbHelper.PLAYER_TABLE_NAME
                        + " WHERE " + DbHelper.PLAYER_COLUMN_ID +  " = ?",
                new String[] { id + "" });
            PlayerDTO deletedPlayer = getResult.getValue();
            return Result.success(deletedPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected void deletePlayerMatches(long id) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(
                    "DELETE FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                            + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID +  " = ?",
                    new String[] { id + "" });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected int getGameCount(long playerId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                            + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID + " = ?",
                    new String[] { playerId + "" });
            return cursor.getCount();
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected List<PlayerDTO> loadAllPlayers() {
        Cursor cursor = null;
        List<PlayerDTO> players = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + DbHelper.PLAYER_TABLE_NAME,
                    null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                PlayerDTO player = cursorToPlayerDTO(cursor);
                players.add(player);
                cursor.moveToNext();
            }
            return players;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @SuppressLint("Range")
    @NonNull
    private static PlayerDTO cursorToPlayerDTO(Cursor cursor) {
        PlayerDTO player = new PlayerDTO();
        player.setId(cursor.getLong(cursor.getColumnIndex(DbHelper.PLAYER_COLUMN_ID)));
        player.setName(cursor.getString(cursor.getColumnIndex(DbHelper.PLAYER_COLUMN_NAME)));
        player.setCreatedAt(cursor.getString(cursor.getColumnIndex(DbHelper.PLAYER_COLUMN_CREATED_AT)));
        player.setUpdatedAt(cursor.getString(cursor.getColumnIndex(DbHelper.PLAYER_COLUMN_UPDATED_AT)));
        return player;
    }
}
