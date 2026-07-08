package com.oplus.systemui.seedlingservice.eventmanager;

import android.os.Bundle;

public class Event {

    private final Bundle data;
    private final String eventCode;

    public Event(String str, Bundle bundle) {
        this.eventCode = str;
        this.data = bundle;
    }

    private final String component1() {
        throw new UnsupportedOperationException("Stub!");
    }

    private final Bundle component2() {
        throw new UnsupportedOperationException("Stub!");
    }


    public final Event copy(String str, Bundle bundle) {
        throw new UnsupportedOperationException("Stub!");
    }

    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final String getEvent() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final Bundle getInfo() {
        throw new UnsupportedOperationException("Stub!");
    }

}