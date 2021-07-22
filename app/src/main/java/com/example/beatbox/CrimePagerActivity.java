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
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity {
    private static final String TAG = "CrimePagerActivity";
    private ViewPager2 vp;
    private List<Crime> mCrimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        SharedPreferences sp = getSharedPreferences(CrimeListActivity.TAG,MODE_PRIVATE);

        int currItemIndex = sp.getInt(CrimeListFragment.SP_INT_ARG_FOR_ITEM_CHANGED,-1);
        Log.d(TAG, "onCreate: "+currItemIndex);
        if(currItemIndex==-1){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null){
                currItemIndex = mCrimes.size();
                mCrimes.add(new Crime(user.getUid()));
                FireBaseDbUtils.addCrime(mCrimes.get(currItemIndex));
            }
        }

        vp = findViewById(R.id.crime_view_pager);
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

    public static Intent CrimeIntent(Context pkgCtx){
        return new Intent(pkgCtx,CrimePagerActivity.class);
    }
}