package com.example.sagi.mines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MinesDB.db";
    public static final String HIGHSCORES_TABLE_NAME = "highScores";
    public static final String HIGHSCORES_COLUMN_ID = "id";
    public static final String HIGHSCORES_COLUMN_NAME = "name";
    //public static final String HIGHSCORES_COLUMN_ADDRESS = "address";
    public static final String HIGHSCORES_COLUMN_LNG = "lng";
    public static final String HIGHSCORES_COLUMN_LAT = "lat";
    public static final String HIGHSCORES_COLUMN_TIME = "time";
    public static final String HIGHSCORES_COLUMN_LEVEL = "level";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table highScores " +
                        "(id integer primary key, name text, lng text, lat text, time text, level integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS highScores");
        onCreate(db);
    }

    public boolean insertHighScore(String name, String lng, String lat, String time, int level)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
//        contentValues.put("address", address);
        contentValues.put("lng", lng);
        contentValues.put("lat", lat);
        contentValues.put("time", time);
        contentValues.put("level", level);
        db.insert("highScores", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from highScores where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, HIGHSCORES_TABLE_NAME);
        return numRows;
    }

    public boolean updateHighScore(Integer id, String name, String lng, String lat, int time, int level)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
//        contentValues.put("address", address);
        contentValues.put("lng", lng);
        contentValues.put("lat", lat);
        contentValues.put("time", time);
        contentValues.put("level", level);
        db.update("highScores", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteHighScore(Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("highScores",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<HashMap<String, String>> getAllHighScores(int level)
    {
        ArrayList<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from highScores where " + HIGHSCORES_COLUMN_LEVEL +" = " + level + " order by time LIMIT 10", null);
        res.moveToFirst();

        while(res.isAfterLast() == false){
            HashMap<String, String> map = new HashMap<>();
            map.put(HIGHSCORES_COLUMN_ID, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_ID)));
            map.put(HIGHSCORES_COLUMN_NAME, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_NAME)));
            //map.put(HIGHSCORES_COLUMN_ADDRESS, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_ADDRESS)));
            map.put(HIGHSCORES_COLUMN_LNG, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_LNG)));
            map.put(HIGHSCORES_COLUMN_LAT, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_LAT)));
            map.put(HIGHSCORES_COLUMN_TIME, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_TIME)));
            map.put(HIGHSCORES_COLUMN_LEVEL, res.getString(res.getColumnIndex(HIGHSCORES_COLUMN_LEVEL)));

            rows.add(map);
            res.moveToNext();
        }
        return rows;
    }

}
