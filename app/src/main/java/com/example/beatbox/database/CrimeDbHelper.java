package com.example.beatbox.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.beatbox.database.CrimeDbSchema.CrimeTable;

import androidx.annotation.Nullable;

public class CrimeDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "crimesDb.db";
    public static final int VERSION = 1;

    public CrimeDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME + "("
                + " _id integer primary key autoincrement unique, "
                + CrimeTable.Cols.UUID + " TEXT, "
                + CrimeTable.Cols.TITLE + " TEXT, "
                + CrimeTable.Cols.DATE + " integer, "
                + CrimeTable.Cols.SOLVED + " integer, "
                + CrimeTable.Cols.USER + " TEXT )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
