package com.example.beatbox;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.beatbox.database.CrimeCursor;
import com.example.beatbox.database.CrimeDbSchema.CrimeTable;

import com.example.beatbox.database.CrimeDbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private SQLiteDatabase mDb;
    private Context mCtx;
    private List<Crime> mCrimes;

    private CrimeLab(Context ctx){
        mCtx = ctx.getApplicationContext();
        mDb = new CrimeDbHelper(mCtx).getWritableDatabase();
        mCrimes = new ArrayList<>();

        /*for(int i=1;i<=3;i++){
            Crime c = new Crime();
            c.setTitle("Crime #"+i);
            c.setSolved(!(i%2==0));
            mCrimes.add(c);
        }*/
    }

    public static CrimeLab get(Context ctx){
        if(sCrimeLab==null)
            sCrimeLab = new CrimeLab(ctx);
        return sCrimeLab;
    }

    private static ContentValues getContentValues(Crime c){
        ContentValues cv = new ContentValues();
        cv.put(CrimeTable.Cols.UUID,c.getId().toString());
        cv.put(CrimeTable.Cols.TITLE,c.getTitle());
        cv.put(CrimeTable.Cols.DATE,c.getDate().getTime());
        cv.put(CrimeTable.Cols.SOLVED,c.isSolved() ? 1 : 0);
        return cv;
    }

    public void addCrime(Crime c){
        ContentValues cv = getContentValues(c);
        mDb.insert(CrimeTable.NAME,null,cv);
        mCrimes.add(c);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        mDb.update(CrimeTable.NAME, values,CrimeTable.Cols.UUID + " = ?", new String[] {uuidString});
    }

    public void deleteCrime(Crime crime) {//removes from database, arrayList and image from pvt app files directory
        int removalIndex = mCrimes.indexOf(crime);
        mCrimes.remove(removalIndex);
        mCtx.getSharedPreferences(CrimeListActivity.TAG,Context.MODE_PRIVATE)
                .edit().putInt(CrimeListFragment.SP_INT_ARG_FOR_ITEM_REMOVED,removalIndex).apply();

        String uuidString = crime.getId().toString();
        mDb.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?", new String[] {uuidString});

        getPhotoFile(crime).delete();
    }

    public File getPhotoFile(Crime crime){
        File dr = mCtx.getFilesDir();
        File f = new File(dr,crime.getPhotoFileName());
        Log.d("CrimeLab getPhotoFile", f.toString());
        return f;
    }

    private CrimeCursor getCursor(String where, String[] whereArgs){
        Cursor cr = mDb.query(CrimeTable.NAME,null,where,whereArgs,null,null,null);
        return new CrimeCursor(cr);
    }

    public List<Crime> getCrimes() {
        CrimeCursor cc = getCursor(null,null);

        mCrimes.clear();
        cc.moveToFirst();
        try {
            while (!cc.isAfterLast()) {
                mCrimes.add(cc.getCrime());
                cc.moveToNext();
            }
        }finally {
            cc.close();
        }

        return mCrimes;
    }
    public Crime getCrime(UUID id) {

        try (CrimeCursor cc = getCursor(CrimeTable.Cols.UUID + " = ?", new String[]{id.toString()})) {
            if (cc.getCount() <= 0) {
                return null;
            }

            cc.moveToFirst();
            return cc.getCrime();
        }
    }
}
