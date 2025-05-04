package com.oplus.epona;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.oplus.epona.route.RouteData;

public class Request implements Parcelable {

    public Request(String str, String str2, Bundle bundle, RouteData routeData) {
        throw new UnsupportedOperationException("Stub!");
    }

    public Request(Parcel parcel) {
        throw new UnsupportedOperationException("Stub!");
    }


    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override
        public Request[] newArray(int size) {
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

    public static class Builder {
        public String mActionName;
        public final Bundle mBundle = new Bundle();
        public String mComponentName;

        public Builder setComponentName(String str) {
            this.mComponentName = str;
            return this;
        }

        public Builder setActionName(String str) {
            this.mActionName = str;
            return this;
        }

        public Builder withParcelable(String str, Parcelable parcelable) {
            this.mBundle.putParcelable(str, parcelable);
            return this;
        }

        public Builder withBundle(String str, Bundle bundle) {
            this.mBundle.putBundle(str, bundle);
            return this;
        }

        public Request build() {
            return new Request(this.mComponentName, this.mActionName, this.mBundle, null);
        }
    }


}
