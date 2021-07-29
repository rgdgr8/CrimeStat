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
import android.widget.Button;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CrimeListFragment extends Fragment {
    private static final String TAG = "CrimeListFrag";
    public static String SP_INT_ARG_FOR_ITEM_CHANGED = "item_changed";
    private RecyclerView rv;
    private CrimeListAdapter clAdapter;
    private SharedPreferences sp;
    private boolean mSubtitleVisible = false;
    private List<Crime> mCrimeList;
    private ValueEventListener valueEventListener;
    private Button userIdText;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sp = getActivity().getSharedPreferences(CrimeListActivity.TAG, Context.MODE_PRIVATE);
        mCrimeList = CrimeLab.get(getActivity()).getCrimes();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: ");
                mCrimeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Crime c = FireBaseDbUtils.getCrime(dataSnapshot);
                    mCrimeList.add(c);
                }
                clAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: ", error.toException());
            }
        };

        FireBaseDbUtils.DB_REF.addValueEventListener(valueEventListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);
        rv = v.findViewById(R.id.crime_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SP_INT_ARG_FOR_ITEM_CHANGED);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userIdText = getActivity().findViewById(R.id.user_id_text);
            userIdText.setText(user.getEmail() + "  " + user.getUid());
        }

        updateUI();

        return v;
    }

    public void updateUI() {
        CrimeLab cl = CrimeLab.get(getActivity());
        clAdapter = new CrimeListAdapter(mCrimeList);
        new ItemTouchHelper(itemCallBack).attachToRecyclerView(rv);
        rv.setAdapter(clAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_crime_list_fragment, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                sp.edit().putInt(SP_INT_ARG_FOR_ITEM_CHANGED, -1).apply();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(getActivity(), "Must login first!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                startActivity(CrimePagerActivity.CrimeIntent(getActivity()));
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();//redraws the toolbar
                updateSubtitle();
                return true;
            case R.id.logout:
                LoginFragment fragment = new LoginFragment();
                FirebaseAuth.getInstance().signOut();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment).commit();
                mCrimeList.clear();
                userIdText.setText("Login or SignUp");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        int crimeCount = mCrimeList.size();
        String subtitle = getString(R.string.subtitle_format, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    /*@Override
    public void onResume() {
        super.onResume();
        int itemChangedChecker = sp.getInt(SP_INT_ARG_FOR_ITEM_CHANGED, -1);
        int itemRemovedChecker = sp.getInt(SP_INT_ARG_FOR_ITEM_REMOVED, -1);

        if (itemChangedChecker > -1) {
            clAdapter.notifyItemChanged(itemChangedChecker);
        }
        if (itemRemovedChecker > -1) {
            clAdapter.notifyItemRemoved(itemRemovedChecker);
            updateSubtitle();
        }
    }*/

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SP_INT_ARG_FOR_ITEM_CHANGED, mSubtitleVisible);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private int position;
        private TextView mTitleTV;
        private TextView mDateTV;
        private ImageView isSolved;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item, parent, false));//super(view)
            mTitleTV = itemView.findViewById(R.id.crime_title);
            mDateTV = itemView.findViewById(R.id.crime_date);
            isSolved = itemView.findViewById(R.id.is_solved);
            itemView.setOnClickListener(this);
        }

        public void bind(Crime c, int pos) {
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
            sp.edit().putInt(SP_INT_ARG_FOR_ITEM_CHANGED, position).apply();
            startActivity(CrimePagerActivity.CrimeIntent(getActivity()));
        }

        public Crime getCrime() {
            return mCrime;
        }
    }

    private class CrimeListAdapter extends RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        public CrimeListAdapter(List<Crime> cl) {
            mCrimes = cl;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new CrimeHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            holder.bind(mCrimes.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }

    ItemTouchHelper.SimpleCallback itemCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || !user.getUid().equals(((CrimeHolder) viewHolder).getCrime().getUserId())) {
                clAdapter.notifyItemChanged(pos);
                return;
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle("Delete?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CrimeHolder ch = (CrimeHolder) viewHolder;
                            CrimeLab.get(getActivity()).deleteCrime(ch.getCrime());
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        FireBaseDbUtils.DB_REF.removeEventListener(valueEventListener);
    }
}
