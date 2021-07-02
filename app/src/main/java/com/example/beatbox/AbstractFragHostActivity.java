package com.example.beatbox;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class AbstractFragHostActivity extends AppCompatActivity {
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.fragment_container);/* This return the appropriate fragment if the fragment's id was already in the fragment manager's list*/
        if(frag==null) {
            frag = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, frag).commit();
        }
    }
}
