package com.damhoe.scoresheetskat.persistence;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.damhoe.scoresheetskat.ApplicationContext;
import com.damhoe.scoresheetskat.DatabaseInfo;

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

   // Game
   public static final String GAME_TABLE_NAME = "game";
   public static final String GAME_COLUMN_ID = "_id";
   public static final String GAME_COLUMN_CREATED_AT = "created_at";
   public static final String GAME_COLUMN_UPDATED_AT = "updated_at";
   public static final String GAME_COLUMN_START_DEALER_POSITION = "start_dealer_position";
   public static final String GAME_COLUMN_TITLE = "title";
   public static final String GAME_COLUMN_SETTINGS_ID = "id_settings";

   // Settings
   public static final String SETTINGS_TABLE_NAME = "settings";
   public static final String SETTINGS_COLUMN_ID = "_id";
   public static final String SETTINGS_COLUMN_NUMBER_ROUNDS = "rounds";
   public static final String SETTINGS_COLUMN_SCORING_TYPE = "scoring";

   // PlayerMatchDTO
   public static final String PLAYER_MATCH_TABLE_NAME = "player_match";
   public static final String PLAYER_MATCH_COLUMN_ID = "_id";
   public static final String PLAYER_MATCH_COLUMN_GAME_ID = "id_game";
   public static final String PLAYER_MATCH_COLUMN_PLAYER_ID = "id_player";
   public static final String PLAYER_MATCH_COLUMN_POSITION = "position";

   // Scores
   public static final String SCORE_TABLE_NAME = "score";
   public static final String SCORE_COLUMN_ID = "_id";
   public static final String SCORE_COLUMN_GAME_ID = "id_game";
   public static final String SCORE_COLUMN_PLAYER_POSITION = "player_position";
   public static final String SCORE_COLUMN_POSITION = "position";
   public static final String SCORE_COLUMN_SPITZEN = "spitzen";
   public static final String SCORE_COLUMN_SUIT = "suit";
   public static final String SCORE_COLUMN_HAND = "hand";
   public static final String SCORE_COLUMN_SCHNEIDER = "schneider";
   public static final String SCORE_COLUMN_SCHNEIDER_ANNOUNCED = "schneider_announced";
   public static final String SCORE_COLUMN_SCHWARZ = "schwarz";
   public static final String SCORE_COLUMN_SCHWARZ_ANNOUNCED = "schwarz_announced";
   public static final String SCORE_COLUMN_RESULT = "result";
   public static final String SCORE_COLUMN_OUVERT = "ouvert";

   @Inject
   public DbHelper(@ApplicationContext Context context,
                   @DatabaseInfo String dbName,
                   @DatabaseInfo Integer version) {
      super(context, dbName, null, version);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      createPlayerTable(db);
      createGameTable(db);
      createScoreTable(db);
      createSettingsTable(db);
      createPlayerMatchTable(db);
   }

   private void createPlayerTable(SQLiteDatabase db) {
      try {
         db.execSQL(
                 "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE_NAME + "("
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

   private void createGameTable(SQLiteDatabase db) {
      try {
         db.execSQL(
                 "CREATE TABLE IF NOT EXISTS " + GAME_TABLE_NAME + "("
                         + GAME_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                         + GAME_COLUMN_CREATED_AT + " VARCHAR(10) DEFAULT CURRENT_TIMESTAMP, "
                         + GAME_COLUMN_UPDATED_AT + " VARCHAR(10) DEFAULT CURRENT_TIMESTAMP, "
                         + GAME_COLUMN_TITLE + " TEXT, "
                         + GAME_COLUMN_SETTINGS_ID + " INTEGER, "
                         + GAME_COLUMN_START_DEALER_POSITION + " INTEGER, "
                         + "FOREIGN KEY (" + GAME_COLUMN_SETTINGS_ID + ")" + " REFERENCES " + SETTINGS_TABLE_NAME + "(" + SETTINGS_COLUMN_ID + "));"
         );
         Log.d("SQL action", "Game table was created.");
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   private void createSettingsTable(SQLiteDatabase db) {
      try {
         db.execSQL(
                 "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE_NAME + "("
                         + SETTINGS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                         + SETTINGS_COLUMN_NUMBER_ROUNDS + " INTEGER, "
                         + SETTINGS_COLUMN_SCORING_TYPE + " INTEGER);"
         );
         Log.d("SQL action", "Settings table was created.");
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   private void createScoreTable(SQLiteDatabase db) {
      try {
         db.execSQL(
                 "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE_NAME + "("
                         + SCORE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                         + SCORE_COLUMN_GAME_ID + " INTEGER, "
                         + SCORE_COLUMN_POSITION + " INTEGER,"
                         + SCORE_COLUMN_PLAYER_POSITION + " INTEGER, "
                         + SCORE_COLUMN_SPITZEN + " INTEGER, "
                         + SCORE_COLUMN_SUIT + " INTEGER, "
                         + SCORE_COLUMN_HAND + " INTEGER, "
                         + SCORE_COLUMN_SCHNEIDER + " INTEGER, "
                         + SCORE_COLUMN_SCHNEIDER_ANNOUNCED + " INTEGER, "
                         + SCORE_COLUMN_SCHWARZ + " INTEGER, "
                         + SCORE_COLUMN_SCHWARZ_ANNOUNCED + " INTEGER, "
                         + SCORE_COLUMN_RESULT + " INTEGER, "
                         + SCORE_COLUMN_OUVERT + " INTEGER, "
                         + "FOREIGN KEY (" + SCORE_COLUMN_GAME_ID + ")" + " REFERENCES " + GAME_TABLE_NAME + "(" + GAME_COLUMN_ID + "), "
                         + "UNIQUE (" + SCORE_COLUMN_GAME_ID + ", " + SCORE_COLUMN_POSITION + "));"
         );
         Log.d("SQL action", "Score table was created.");
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   private void createPlayerMatchTable(SQLiteDatabase db) {
      try {
         db.execSQL(
                 "CREATE TABLE IF NOT EXISTS " + PLAYER_MATCH_TABLE_NAME + "("
                         + PLAYER_MATCH_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                         + PLAYER_MATCH_COLUMN_GAME_ID + " INTEGER, "
                         + PLAYER_MATCH_COLUMN_PLAYER_ID + " INTEGER, "
                         + PLAYER_MATCH_COLUMN_POSITION + " INTEGER, "
                         + "FOREIGN KEY (" + PLAYER_MATCH_COLUMN_GAME_ID + ")" + " REFERENCES " + GAME_TABLE_NAME + "(" + GAME_COLUMN_ID + "), "
                         + "FOREIGN KEY (" + PLAYER_MATCH_COLUMN_PLAYER_ID + ")" + " REFERENCES " + PLAYER_TABLE_NAME + "(" + PLAYER_COLUMN_ID + "), "
                         + "UNIQUE (" + PLAYER_MATCH_COLUMN_GAME_ID + ", " + PLAYER_MATCH_COLUMN_POSITION + "));"
         );
         Log.d("SQL action", "PlayerMatchDTO table was created.");
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int newVersion, int oldVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + PLAYER_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + GAME_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + SCORE_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME);
      db.execSQL("DROP TABLE IF EXISTS " + PLAYER_MATCH_TABLE_NAME);
      onCreate(db);
   }
}
