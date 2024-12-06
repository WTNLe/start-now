package com.example.startnownew;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class HabitDBHelper extends SQLiteOpenHelper {
    // 数据库常量
    private static final String DATABASE_NAME = "habitdb";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "habits";

    // 列名常量
    private static final String COLUMN_NAME = "habit_name";
    private static final String COLUMN_COMPLETED = "completed";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_ADDRESS = "address";

    public HabitDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME +
                "(" + COLUMN_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_COMPLETED + " INTEGER, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_ADDRESS + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String alterTableQuery = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_ADDRESS + " TEXT";
            db.execSQL(alterTableQuery);
        }
    }

    // 插入习惯记录
    public void insertHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, habit.getName());
        values.put(COLUMN_COMPLETED, habit.isCompleted() ? 1 : 0);

        if (habit.getLatitude() != null && habit.getLongitude() != null) {
            values.put(COLUMN_LATITUDE, habit.getLatitude());
            values.put(COLUMN_LONGITUDE, habit.getLongitude());
        }

        if (habit.getAddress() != null) {
            values.put(COLUMN_ADDRESS, habit.getAddress());
        }

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // 更新习惯记录
    public void updateHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, habit.isCompleted() ? 1 : 0);

        if (habit.getLatitude() != null && habit.getLongitude() != null) {
            values.put(COLUMN_LATITUDE, habit.getLatitude());
            values.put(COLUMN_LONGITUDE, habit.getLongitude());
        }

        if (habit.getAddress() != null) {
            values.put(COLUMN_ADDRESS, habit.getAddress());
        }

        db.update(TABLE_NAME, values, COLUMN_NAME + "=?", new String[]{habit.getName()});
        db.close();
    }

    // 删除习惯记录
    public void deleteHabit(String habitName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_NAME + "=?", new String[]{habitName});
        db.close();
    }

    // 获取所有习惯记录
    public List<Habit> getAllHabits() {
        List<Habit> habitList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String habitName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                boolean completed = cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETED)) == 1;
                Double latitude = cursor.isNull(cursor.getColumnIndex(COLUMN_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                Double longitude = cursor.isNull(cursor.getColumnIndex(COLUMN_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));

                Habit habit = new Habit(habitName);
                habit.setCompleted(completed);
                habit.setLatitude(latitude);
                habit.setLongitude(longitude);
                habit.setAddress(address);

                habitList.add(habit);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        db.close();
        return habitList;
    }

    // 重置所有习惯的完成状态
    public void resetAllHabitsCompletion() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMPLETED, 0);
        db.update(TABLE_NAME, values, null, null);
        db.close();
    }
}
