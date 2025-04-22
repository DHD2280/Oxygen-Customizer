package com.oplus.basecommon.sort;

import android.os.UserHandle;

import java.util.Comparator;

public class AppNameComparator implements Comparator<AppNameComparator.IAppOrderInfo> {

    @Override
    public int compare(IAppOrderInfo o1, IAppOrderInfo o2) {
        throw new UnsupportedOperationException("STUB");
    }

    public interface IAppOrderInfo {
        int getAlphabeticIndex();

        char getInitialChar();

        String getOrderInfo();

        String getOrderInfo2();

        UserHandle getUser();
    }


}
