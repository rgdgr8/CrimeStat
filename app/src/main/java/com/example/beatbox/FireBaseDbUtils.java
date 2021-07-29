package com.example.beatbox;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

public class FireBaseDbUtils {
    private static final String CRIMES = "crimes";
    private static final String USER_ID = "user_id";
    private static final String TITLE = "title";
    private static final String DATE = "date";
    private static final String SOLVED = "is_solved";
    private static final String TAG = "FireDbUtils";
    public static final DatabaseReference DB_REF = FirebaseDatabase.getInstance().getReference(CRIMES);
    public static final String FB_IMAGES = "images";

    public static Crime getCrime(DataSnapshot snapshot) {
        UUID uuid = UUID.fromString(snapshot.getKey());
        String userId = snapshot.child(USER_ID).getValue(String.class);
        Crime c = new Crime(uuid, userId);

        String title = snapshot.child(TITLE).getValue(String.class);
        try {
            Date date = new Date(snapshot.child(DATE).getValue(Long.class));
            c.setDate(date);

            boolean solved = snapshot.child(SOLVED).getValue(Boolean.class);
            c.setSolved(solved);
        } catch (Exception e) {
            e.printStackTrace();
        }

        c.setTitle(title);

        return c;
    }

    public static void deleteCrime(Crime crime) {
        Log.d(TAG, "deleteCrime: ");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            boolean del = DB_REF.child(crime.getId().toString()).setValue(null).isSuccessful();
            Log.d(TAG, "deleteCrime: " + del);
        }

        deletePhotoFile(crime);
    }

    public static void updateCrime(Crime crime, boolean add) {
        Log.d(TAG, "updateCrime: ");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference dbRefChild = DB_REF.child(crime.getId().toString());
            if (add) {
                dbRefChild.child(USER_ID).setValue(user.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e(TAG, "onCompleteUserId: ", task.getException());
                    }
                });
            }
            dbRefChild.child(TITLE).setValue(crime.getTitle()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.e(TAG, "onCompleteTitle: ", task.getException());
                }
            });
            dbRefChild.child(DATE).setValue(crime.getDate().getTime()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.e(TAG, "onCompleteDate: ", task.getException());
                }
            });
            dbRefChild.child(SOLVED).setValue(crime.isSolved()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.e(TAG, "onCompleteSolved: ", task.getException());
                }
            });
        }
    }

    public static void addCrime(Crime crime) {
        Log.d(TAG, "addCrime: ");
        updateCrime(crime, true);
    }

    public static void uploadPhotoFile(File f) {
        Uri file = Uri.fromFile(f);
        String path = FB_IMAGES + "/" + file.getLastPathSegment();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);
        ref.getDownloadUrl().addOnFailureListener(e -> {
            UploadTask uploadTask = ref.putFile(file);
            uploadTask.addOnFailureListener(exception -> Log.e(TAG, "PhotoUploadFailure: ", exception));
        });
    }

    public static void deletePhotoFile(Crime crime) {
        String path = FB_IMAGES + "/" + crime.getPhotoFileName();
        FirebaseStorage.getInstance().getReference().child(path).delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onDeletePhotoFailure: ", e);
            }
        });
    }
}
