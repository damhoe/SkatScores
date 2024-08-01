package com.damhoe.skatscores.game.adapter.repositories;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.persistence.DbHelper;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PlayerMatchPersistenceAdapter {
   private final DbHelper dbHelper;

   @Inject
   public PlayerMatchPersistenceAdapter(DbHelper dbHelper) {
      this.dbHelper = dbHelper;
   }

   /** @noinspection UnusedReturnValue*/
   protected long insertPlayerMatch(PlayerMatchDTO playerMatch) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         ContentValues contentValues = new ContentValues();
         contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_GAME_ID, playerMatch.getGameId());
         contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID, playerMatch.getPlayerId());
         contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_POSITION, playerMatch.getPosition());
         return db.insert(DbHelper.PLAYER_MATCH_TABLE_NAME, null, contentValues);
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   @SuppressLint("Range")
   protected Result<PlayerMatchDTO> getPlayerMatch(long id) {
      Cursor cursor = null;
      try {
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         cursor = db.rawQuery(
                 "SELECT * FROM "
                         + DbHelper.PLAYER_MATCH_TABLE_NAME
                         + " WHERE "
                         + DbHelper.PLAYER_MATCH_COLUMN_ID + " = ? ",
                 new String[] {String.valueOf(id)});
         if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            PlayerMatchDTO player = cursorToPlayerMatchDTO(cursor);
            return Result.success(player);
         } else {
            return Result.failure("PlayerMatch with id " + id + " does not exist");
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

   protected int updatePlayerMatch(PlayerMatchDTO playerMatch) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         ContentValues contentValues = new ContentValues();
         contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_GAME_ID, playerMatch.getGameId());
         contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID, playerMatch.getPlayerId());
         contentValues.put(DbHelper.PLAYER_MATCH_COLUMN_POSITION, playerMatch.getPosition());
         String whereClause = DbHelper.PLAYER_MATCH_COLUMN_ID + " = ? ";
         return db.update(DbHelper.PLAYER_MATCH_TABLE_NAME, contentValues, whereClause,
                 new String[] { String.valueOf(playerMatch.getId()) });
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   protected Result<PlayerMatchDTO> deletePlayerMatch(long id) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         Result<PlayerMatchDTO> getResult = getPlayerMatch(id);
         if (getResult.isFailure()) {
            return Result.failure("PlayerMatch with id " + id + " was not deleted because it did not exist");
         }
         db.execSQL(
                 "DELETE FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                         + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_ID +  " = ?",
                 new String[] { id + "" });
         PlayerMatchDTO deletedPlayer = getResult.value;
         return Result.success(deletedPlayer);
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   protected boolean isValidPlayerId(long playerId) {
      Cursor cursor = null;
      try {
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         cursor = db.rawQuery(
                 "SELECT * FROM " + DbHelper.PLAYER_TABLE_NAME
                         + " WHERE " + DbHelper.PLAYER_COLUMN_ID + " = ? ",
                 new String[] {String.valueOf(playerId)});
         return cursor.getCount() > 0;
      } catch (NullPointerException e) {
         e.printStackTrace();
         throw e;
      } finally {
         if (cursor != null) {
            cursor.close();
         }
      }
   }

   protected Result<List<PlayerMatchDTO>> deletePlayerMatchesForGame(long gameId) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         List<PlayerMatchDTO> matches = loadPlayerMatches(gameId);
         if (matches.size() > 0) {
            db.execSQL("DELETE FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                 + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_GAME_ID +  " = ?",
                 new String[] { gameId + "" });
         }
         return Result.success(matches);
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   protected List<PlayerMatchDTO> loadPlayerMatches(long gameId) {
      Cursor cursor = null;
      List<PlayerMatchDTO> matches = new ArrayList<>();
      try {
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         cursor = db.rawQuery(
                 "SELECT * FROM " + DbHelper.PLAYER_MATCH_TABLE_NAME
                         + " WHERE " + DbHelper.PLAYER_MATCH_COLUMN_GAME_ID + " = ? ",
                 new String[] { gameId + "" });
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
            PlayerMatchDTO playerMatch = cursorToPlayerMatchDTO(cursor);
            matches.add(playerMatch);
            cursor.moveToNext();
         }
         return matches;
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
   private static PlayerMatchDTO cursorToPlayerMatchDTO(Cursor cursor) {
      int idIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_ID);
      int gameIdIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_GAME_ID);
      int playerIdIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_PLAYER_ID);
      int positionIndex = cursor.getColumnIndex(DbHelper.PLAYER_MATCH_COLUMN_POSITION);

      long id = cursor.getLong(idIndex);
      long gameId = cursor.getLong(gameIdIndex);
      long playerId = cursor.getLong(playerIdIndex);
      int position = cursor.getInt(positionIndex);

      return new PlayerMatchDTO(id, gameId, playerId, position);
   }
}