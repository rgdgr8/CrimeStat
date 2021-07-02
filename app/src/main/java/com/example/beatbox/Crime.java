package com.example.beatbox;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private final UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }
    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public void setSolved(boolean mSolved) {
        this.mSolved = mSolved;
    }

    public String getTitle() {
        return mTitle;
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isSolved() {
        return mSolved;
    }

    @Override
    public boolean equals(Object o){
        if(o==null)
            return false;
        if (o instanceof Crime){
            Crime c = (Crime)o;
            return c.mId.equals(this.mId);
        }
        return false;
    }

    public String getPhotoFileName(){
        return "IMG_"+getId()+".jpg";
    }

    @NonNull
    @Override
    public String toString() {
        return "Crime Report:" +
                "\nTitle: " + mTitle +
                "\nDate: " + mDate +
                "\nSolved: " + (mSolved ? "Yes" : "No");
    }
}

