package com.damhoe.scoresheetskat.persistance;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.damhoe.scoresheetskat.ApplicationContext;
import com.damhoe.scoresheetskat.DatabaseInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DbHelper extends SQLiteOpenHelper {

   // Player
   public static final String PLAYER_TABLE_NAME = "players";
   public static final String PLAYER_COLUMN_ID = "_id";
   public static final String PLAYER_COLUMN_NAME = "name";
   public static final String PLAYER_COLUMN_CREATED_AT = "created_at";
   public static final String PLAYER_COLUMN_UPDATED_AT = "updated_at";

   @Inject
   public DbHelper(@ApplicationContext Context context,
                   @DatabaseInfo String dbName,
                   @DatabaseInfo Integer version) {
      super(context, dbName, null, version);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      createPlayerTable(db);
   }

   private void createPlayerTable(SQLiteDatabase db) {
      try {
         db.execSQL(
                 "CREATE TABLE IF NOT EXISTS "
                         + PLAYER_TABLE_NAME + "("
                         + PLAYER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                         + PLAYER_COLUMN_NAME + " VARCHAR(20), "
                         + PLAYER_COLUMN_CREATED_AT + " VARCHAR(10) DEFAULT CURRENT_TIMESTAMP, "
                         + PLAYER_COLUMN_UPDATED_AT + " VARCHAR(10) DEFAULT CURRENT_TIMESTAMP);"
         );
         Log.d("SQL action", "Player table was created.");
      } catch (SQLException ex) {
         ex.printStackTrace();
      }
   }

   private String getCurrentTimeStamp() {
      return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
              .format(Calendar.getInstance().getTime());
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int newVersion, int oldVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + PLAYER_TABLE_NAME);
      onCreate(db);
   }
}
