package com.example.beatbox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    public static String SP_INT_ARG_FOR_ITEM_CHANGED = "item_changed";
    public static String SP_INT_ARG_FOR_ITEM_REMOVED = "item_removed";
    private RecyclerView rv;
    private CrimeListAdapter clAdapter;
    private SharedPreferences sp;
    private boolean mSubtitleVisible = false;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler_view,container,false);
        rv = v.findViewById(R.id.crime_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SP_INT_ARG_FOR_ITEM_CHANGED);
        }
        updateUI();

        return v;
    }

    public void updateUI(){
        CrimeLab cl = CrimeLab.get(getActivity());
        clAdapter = new CrimeListAdapter(cl.getCrimes());
        new ItemTouchHelper(itemCallBack).attachToRecyclerView(rv);
        rv.setAdapter(clAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list,menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_crime:
                CrimeLab cl = CrimeLab.get(getActivity());
                sp.edit().putInt(SP_INT_ARG_FOR_ITEM_CHANGED,cl.getCrimes().size()).apply();//saves the position of the new list item
                Crime c = new Crime();
                cl.addCrime(c);
                clAdapter.notifyItemInserted((cl.getCrimes().size()-1));
                startActivity(CrimePagerActivity.CrimeIntent(getActivity(),c.getId()));
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();//redraws the toolbar
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        sp = getActivity().getSharedPreferences(CrimeListActivity.TAG, Context.MODE_PRIVATE);
        int itemChangedChecker = sp.getInt(SP_INT_ARG_FOR_ITEM_CHANGED,-1);
        int itemRemovedChecker = sp.getInt(SP_INT_ARG_FOR_ITEM_REMOVED,-1);

        if(itemChangedChecker>-1) {
            clAdapter.notifyItemChanged(itemChangedChecker);
        }
        if(itemRemovedChecker>-1){
            clAdapter.notifyItemRemoved(itemRemovedChecker);
            updateSubtitle();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SP_INT_ARG_FOR_ITEM_CHANGED,mSubtitleVisible);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private int position;
        private TextView mTitleTV;
        private TextView mDateTV;
        private ImageView isSolved;
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item,parent,false));//super(view)
            mTitleTV = itemView.findViewById(R.id.crime_title);
            mDateTV = itemView.findViewById(R.id.crime_date);
            isSolved = itemView.findViewById(R.id.is_solved);
            itemView.setOnClickListener(this);
        }

        public void bind(Crime c,int pos){
            mCrime = c;
            position = pos;
            mTitleTV.setText(mCrime.getTitle());
            mDateTV.setText(mCrime.getDate().toString());
            if (mCrime.isSolved())
                isSolved.setVisibility(View.VISIBLE);
            else
                isSolved.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(getActivity(),mCrime.getTitle(),Toast.LENGTH_SHORT).show();
            sp.edit().putInt(SP_INT_ARG_FOR_ITEM_CHANGED,position).apply();
            startActivity(CrimePagerActivity.CrimeIntent(getActivity(),mCrime.getId()));
        }

        public Crime getCrime(){return mCrime;}
    }

    private class CrimeListAdapter extends RecyclerView.Adapter<CrimeHolder>{
        private List<Crime> mCrimes;
        public CrimeListAdapter(List<Crime> cl){
            mCrimes = cl;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(inflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            holder.bind(mCrimes.get(position),position);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }

    ItemTouchHelper.SimpleCallback itemCallBack = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            new AlertDialog.Builder(getActivity())
                    .setTitle("Delete?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CrimeHolder ch = (CrimeHolder)viewHolder;
                            CrimeLab.get(getActivity()).deleteCrime(ch.getCrime());
                            clAdapter.notifyItemRemoved(pos);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clAdapter.notifyItemChanged(pos);
                        }
                    })
                    .create().show();
        }
    };
}
