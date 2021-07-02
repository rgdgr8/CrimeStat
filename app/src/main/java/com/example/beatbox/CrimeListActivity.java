package com.example.beatbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class CrimeListActivity extends AbstractFragHostActivity {
    public static String TAG = "Crime List Activity";

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}