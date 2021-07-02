package com.example.beatbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    public static String ARG_DATE = "date";
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.date_picker,null);
        DatePicker mDatePicker = (DatePicker) v.findViewById(R.id.date_picker);
        mDatePicker.init(year, month, day, null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Pick a date")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                        Date date = new GregorianCalendar(year, month, day).getTime();
                        sendResult(date);
                    }
                })
                .create();
    }

    private void sendResult(Date date) {
        FragmentManager fm = getParentFragmentManager();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DATE,date);
        fm.setFragmentResult(CrimeFragment.RESULT_DATE,b);
    }

    public static DatePickerFragment newInstance(Date dt){
        Bundle b = new Bundle();
        b.putSerializable(ARG_DATE,dt);
        DatePickerFragment dpf = new DatePickerFragment();
        dpf.setArguments(b);
        return dpf;
    }
}
