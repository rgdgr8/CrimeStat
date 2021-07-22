package com.example.beatbox;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static final String TAG = "CrimeLab";
    private static CrimeLab sCrimeLab;
    //private final SQLiteDatabase mDb;
    private final Context mCtx;
    private final List<Crime> mCrimes;

    private CrimeLab(Context ctx) {
        mCtx = ctx.getApplicationContext();
        mCrimes = new ArrayList<>();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //mDb = new CrimeDbHelper(mCtx).getWritableDatabase();
        /*CrimeCursor cc = getCursor(null, null);
        cc.moveToFirst();
        try {
            while (!cc.isAfterLast()) {
                Crime c = cc.getCrime();
                mCrimes.add(c);
                //FireBaseDbUtils.updateCrime(c);
                cc.moveToNext();
            }
        } finally {
            cc.close();
        }*/
    }

    public static CrimeLab get(Context ctx) {
        if (sCrimeLab == null)
            sCrimeLab = new CrimeLab(ctx);
        return sCrimeLab;
    }

    /*private static ContentValues getContentValues(Crime c) {
        ContentValues cv = new ContentValues();
        cv.put(CrimeTable.Cols.UUID, c.getId().toString());
        cv.put(CrimeTable.Cols.TITLE, c.getTitle());
        cv.put(CrimeTable.Cols.DATE, c.getDate().getTime());
        cv.put(CrimeTable.Cols.SOLVED, c.isSolved() ? 1 : 0);
        cv.put(CrimeTable.Cols.USER, c.getUserId());

        return cv;
    }*/

    public void addCrime(Crime c) {
        //mCrimes.add(c);

        //ContentValues cv = getContentValues(c);
        //mDb.insert(CrimeTable.NAME, null, cv);

        FireBaseDbUtils.addCrime(c);
    }

    public void updateCrime(Crime crime) {
        //String uuidString = crime.getId().toString();
        //ContentValues values = getContentValues(crime);
        //mDb.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[]{uuidString});

        FireBaseDbUtils.updateCrime(crime,false);
    }

    public void deleteCrime(Crime crime) {//removes from database, arrayList and image from pvt app files directory
        //mCrimes.remove(removalIndex);

        /*String uuidString = crime.getId().toString();
        mDb.delete(CrimeTable.NAME,
                CrimeTable.Cols.UUID + " = ?", new String[]{uuidString});*/

        getPhotoFile(crime).delete();

        FireBaseDbUtils.deleteCrime(crime);
    }

    public File getPhotoFile(Crime crime) {
        File dr = mCtx.getFilesDir();
        File f = new File(dr, crime.getPhotoFileName());
        Log.d("CrimeLab getPhotoFile", f.toString());
        return f;
    }

    /*private CrimeCursor getCursor(String where, String[] whereArgs) {
        Cursor cr = mDb.query(CrimeTable.NAME, null, where, whereArgs, null, null, null);
        return new CrimeCursor(cr);
    }*/

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrime(UUID id) {

        /*try (CrimeCursor cc = getCursor(CrimeTable.Cols.UUID + " = ?", new String[]{id.toString()})) {
            if (cc.getCount() <= 0) {
                return null;
            }

            cc.moveToFirst();
            return cc.getCrime();
        }*/

        for (Crime c : mCrimes) {
            if (c.getId() == id)
                return c;
        }
        return null;
    }
}
