package com.example.beatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {
    public static String INTENT_EXTRA_CRIME_ID = "com.example.beatbox.crime_id";
    private ViewPager2 vp;
    private List<Crime> mCrimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        SharedPreferences sp = getSharedPreferences(CrimeListActivity.TAG,MODE_PRIVATE);
        int currItemIndex = sp.getInt(CrimeListFragment.SP_INT_ARG_FOR_ITEM_CHANGED,0);

        vp = findViewById(R.id.crime_view_pager);
        mCrimes = CrimeLab.get(this).getCrimes();

        vp.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return CrimeFragment.newInstance(mCrimes.get(position).getId());
            }

            @Override
            public int getItemCount() {
                return mCrimes.size();
            }
        });

        vp.setCurrentItem(currItemIndex);
    }

    public static Intent CrimeIntent(Context pkgCtx, UUID id){
        Intent i = new Intent(pkgCtx,CrimePagerActivity.class);
        i.putExtra(INTENT_EXTRA_CRIME_ID,id);
        return i;
    }
}