package com.example.beatbox.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.example.beatbox.database.CrimeDbSchema.CrimeTable;

import com.example.beatbox.Crime;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class CrimeCursor extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public CrimeCursor(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime(){
        String uuid = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        boolean isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED)) != 0;

        Crime c = new Crime(UUID.fromString(uuid));
        c.setTitle(title);
        c.setDate(new Date(date));
        c.setSolved(isSolved);

        return c;
    }
}
