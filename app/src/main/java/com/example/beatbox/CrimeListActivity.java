package com.example.beatbox;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CrimeListActivity extends AppCompatActivity {
    public static final String TAG = "Crime List Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            Log.d(TAG, "onCreate: fragment = null");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user == null)
                fragment = new LoginFragment();
            else
                fragment = new CrimeListFragment();

            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        } else {
            Log.d(TAG, "onCreate: fragment = " + fragment.toString());
        }
    }
}