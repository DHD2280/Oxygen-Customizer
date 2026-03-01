package com.oplus.epona;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Response implements Parcelable {

    public static final Creator<Response> CREATOR = new Creator<Response>() {
        @Override
        public Response createFromParcel(Parcel in) {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override
        public Response[] newArray(int size) {
            throw new UnsupportedOperationException("Stub!");
        }
    };

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        throw new UnsupportedOperationException("Stub!");
    }

    public Response(int i2, String str) {
        throw new UnsupportedOperationException("Stub!");
    }

    public Response(Parcel parcel) {
        throw new UnsupportedOperationException("Stub!");
    }

    public boolean isSuccessful() {
        throw new UnsupportedOperationException("Stub!");
    }

    public Bundle getBundle() {
        throw new UnsupportedOperationException("Stub!");
    }

    public String getMessage() {
        throw new UnsupportedOperationException("Stub!");
    }


}
