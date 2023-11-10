package com.damhoe.scoresheetskat.score.adapter.out;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.damhoe.scoresheetskat.base.Result;
import com.damhoe.scoresheetskat.persistance.DbHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.damhoe.scoresheetskat.persistance.Utility.getIntFromCursor;
import static com.damhoe.scoresheetskat.persistance.Utility.getLongFromCursor;

@Singleton
public class ScorePersistenceAdapter {
    private final DbHelper mDbHelper;

    @Inject
    public ScorePersistenceAdapter(DbHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    public long insertScore(ScoreDto score) {
        Cursor cursor = null;
        try {

            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Get score position
            int scorePosition = 0;
            String maxScoreKey = "max_score";

            cursor = db.rawQuery(
                    "SELECT MAX(" + DbHelper.SCORE_COLUMN_POSITION + ") AS " + maxScoreKey
                            + " FROM " + DbHelper.SCORE_TABLE_NAME
                            + " WHERE " + DbHelper.SCORE_COLUMN_GAME_ID + " = ?",
                    new String[] { score.getGameId() + "" }
            );
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                scorePosition = getIntFromCursor(cursor, maxScoreKey, -1) + 1;
            }

            // Save data
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.SCORE_COLUMN_GAME_ID, score.getGameId());
            contentValues.put(DbHelper.SCORE_COLUMN_POSITION, scorePosition);
            contentValues.put(DbHelper.SCORE_COLUMN_PLAYER_POSITION, score.getPlayerPosition());
            contentValues.put(DbHelper.SCORE_COLUMN_SPITZEN, score.getSpitzen());
            contentValues.put(DbHelper.SCORE_COLUMN_SUIT, score.getSuit());
            contentValues.put(DbHelper.SCORE_COLUMN_HAND, score.getHand());
            contentValues.put(DbHelper.SCORE_COLUMN_OUVERT, score.getOuvert());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHNEIDER, score.getSchneider());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHNEIDER_ANNOUNCED, score.getSchneiderAnnounced());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHWARZ, score.getSchwarz());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHWARZ_ANNOUNCED, score.getSchwarzAnnounced());
            contentValues.put(DbHelper.SCORE_COLUMN_RESULT, score.getResult());

            return db.insert(DbHelper.SCORE_TABLE_NAME, null, contentValues);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Result<ScoreDto> updateScore(ScoreDto score) {
        try {
            // Score position remains the same
            // because scores remain in a fixed order
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Save data
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHelper.SCORE_COLUMN_GAME_ID, score.getGameId());
            contentValues.put(DbHelper.SCORE_COLUMN_PLAYER_POSITION, score.getPlayerPosition());
            contentValues.put(DbHelper.SCORE_COLUMN_SPITZEN, score.getSpitzen());
            contentValues.put(DbHelper.SCORE_COLUMN_SUIT, score.getSuit());
            contentValues.put(DbHelper.SCORE_COLUMN_HAND, score.getHand());
            contentValues.put(DbHelper.SCORE_COLUMN_OUVERT, score.getOuvert());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHNEIDER, score.getSchneider());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHNEIDER_ANNOUNCED, score.getSchneiderAnnounced());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHWARZ, score.getSchwarz());
            contentValues.put(DbHelper.SCORE_COLUMN_SCHWARZ_ANNOUNCED, score.getSchwarzAnnounced());
            contentValues.put(DbHelper.SCORE_COLUMN_RESULT, score.getResult());
            String whereClause = DbHelper.SCORE_COLUMN_ID + " = " + score.getId();
            boolean success = db.update(
                    DbHelper.SCORE_TABLE_NAME,
                    contentValues,
                    whereClause, null
            ) == 1;

            if (success) {
                return Result.success(score);
            }
            return Result.failure("Unable to update score with id: " + score.getId());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public List<ScoreDto> getScoresForGame(long gameId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + DbHelper.SCORE_TABLE_NAME
                            + " WHERE " + DbHelper.SCORE_COLUMN_GAME_ID + " = ?"
                            + " ORDER BY " + DbHelper.SCORE_COLUMN_POSITION + " ASC;",
                    new String[] { gameId + "" }
            );
            List<ScoreDto> scores = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                scores.add(cursorToScoreDto(cursor));
                cursor.moveToNext();
            }
            return scores;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Result<ScoreDto> getScore(long id) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + DbHelper.SCORE_TABLE_NAME
                            + " WHERE " + DbHelper.SCORE_COLUMN_ID + " = ?",
                    new String[] { id + "" }
            );
            if (cursor.getCount() < 1) {
                return Result.failure("Score with id " + id + " does not exist.");
            }
            cursor.moveToFirst();
            return Result.success(cursorToScoreDto(cursor));
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Result<ScoreDto> deleteScore(long id) {
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Result<ScoreDto> getResult = getScore(id);
            if (getResult.isSuccess()) {
                db.execSQL(
                        "DELETE FROM " + DbHelper.SCORE_TABLE_NAME
                                + " WHERE " + DbHelper.SCORE_COLUMN_ID + " =? ",
                        new String[] { id + "" }
                );
            }
            return getResult;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void deleteScoresForGame(long gameId) {
        try {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            db.execSQL(
                    "DELETE FROM " + DbHelper.SCORE_TABLE_NAME
                            + " WHERE " + DbHelper.SCORE_COLUMN_GAME_ID + " =? ",
                    new String[] { gameId + "" }
            );
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @SuppressLint("Range")
    @NonNull
    private static ScoreDto cursorToScoreDto(Cursor cursor) {
        ScoreDto score = new ScoreDto();
        score.setId(getLongFromCursor(cursor, DbHelper.SCORE_COLUMN_ID, -1));
        score.setGameId(getLongFromCursor(cursor, DbHelper.SCORE_COLUMN_GAME_ID, -1));
        score.setPlayerPosition(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_PLAYER_POSITION, 0));
        score.setSpitzen(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_SPITZEN, 1));
        score.setSuit(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_SUIT, 1));
        score.setHand(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_HAND, 0));
        score.setOuvert(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_OUVERT, 0));
        score.setSchneider(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_SCHNEIDER, 0));
        score.setSchneiderAnnounced(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_SCHNEIDER_ANNOUNCED, 0));
        score.setSchwarz(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_SCHWARZ, 0));
        score.setSchwarzAnnounced(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_SCHWARZ_ANNOUNCED, 0));
        score.setResult(getIntFromCursor(cursor, DbHelper.SCORE_COLUMN_RESULT, 0));
        return score;
    }
}
