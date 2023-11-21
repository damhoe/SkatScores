package com.damhoe.skatscores.game.adapter.out;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.damhoe.skatscores.base.Result;
import com.damhoe.skatscores.game.adapter.out.models.SkatSettingsDTO;
import com.damhoe.skatscores.persistence.DbHelper;

import javax.inject.Inject;

import static com.damhoe.skatscores.persistence.Utility.getIntFromCursor;
import static com.damhoe.skatscores.persistence.Utility.getLongFromCursor;

public class SettingsPersistenceAdapter {
   private final DbHelper dbHelper;

   @Inject
   public SettingsPersistenceAdapter(DbHelper dbHelper) {
      this.dbHelper = dbHelper;
   }

   protected long insertSettings(SkatSettingsDTO settingsDTO) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         ContentValues contentValues = new ContentValues();
         contentValues.put(DbHelper.SETTINGS_COLUMN_SCORING_TYPE, settingsDTO.getScoringType());
         contentValues.put(DbHelper.SETTINGS_COLUMN_NUMBER_ROUNDS, settingsDTO.getNumberOfRounds());
         return db.insert(DbHelper.SETTINGS_TABLE_NAME, null, contentValues);
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   @SuppressLint("Range")
   protected Result<SkatSettingsDTO> getSettings(long id) {
      Cursor cursor = null;
      try {
         SQLiteDatabase db = dbHelper.getReadableDatabase();
         cursor = db.rawQuery(
                 "SELECT * FROM " + DbHelper.SETTINGS_TABLE_NAME
                         + " WHERE " + DbHelper.SETTINGS_COLUMN_ID + " = ? ",
                 new String[] {String.valueOf(id)});
         if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            SkatSettingsDTO settingsDTO = cursorToSkatSettingsDTO(cursor);
            return Result.success(settingsDTO);
         } else {
            return Result.failure("Settings with id " + id + " does not exist");
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

   protected int updateSettings(SkatSettingsDTO settingsDTO) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         ContentValues contentValues = new ContentValues();
         contentValues.put(DbHelper.SETTINGS_COLUMN_NUMBER_ROUNDS, settingsDTO.getNumberOfRounds());
         contentValues.put(DbHelper.SETTINGS_COLUMN_SCORING_TYPE, settingsDTO.getScoringType());
         String whereClause = DbHelper.SETTINGS_COLUMN_ID + " = ? ";
         return db.update(DbHelper.SETTINGS_TABLE_NAME, contentValues, whereClause, new String[] { String.valueOf(settingsDTO.getId()) });
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   protected Result<SkatSettingsDTO> deleteSettings(long id) {
      try {
         SQLiteDatabase db = dbHelper.getWritableDatabase();
         Result<SkatSettingsDTO> getResult = getSettings(id);
         if (getResult.isFailure()) {
            return Result.failure("Settings with id " + id + " were not deleted because it did not exist");
         }
         db.execSQL(
                 "DELETE FROM " + DbHelper.SETTINGS_TABLE_NAME
                         + " WHERE " + DbHelper.SETTINGS_COLUMN_ID +  " = ?",
                 new String[] { id + "" });
         SkatSettingsDTO deletedSettings = getResult.value;
         return Result.success(deletedSettings);
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   private static SkatSettingsDTO cursorToSkatSettingsDTO(Cursor cursor) {
      SkatSettingsDTO settingsDTO = new SkatSettingsDTO();

      long id = getLongFromCursor(cursor, DbHelper.SETTINGS_COLUMN_ID, -1);
      int numberOfRounds = getIntFromCursor(cursor, DbHelper.SETTINGS_COLUMN_NUMBER_ROUNDS, 0);
      int scoringType = getIntFromCursor(cursor, DbHelper.SETTINGS_COLUMN_SCORING_TYPE, 0);

      settingsDTO.setId(id);
      settingsDTO.setScoringType(scoringType);
      settingsDTO.setNumberOfRounds(numberOfRounds);

      return settingsDTO;
   }
}
