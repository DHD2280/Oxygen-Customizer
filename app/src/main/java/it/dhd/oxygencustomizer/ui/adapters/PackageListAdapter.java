package it.dhd.oxygencustomizer.ui.adapters;

/*
 * Copyright (C) 2012-2014 The CyanogenMod Project
 *               2022 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import it.dhd.oxygencustomizer.R;

public class PackageListAdapter extends BaseAdapter implements Runnable {

    private final PackageManager mPm;
    private final LayoutInflater mInflater;
    private final List<PackageItem> mInstalledPackages = new LinkedList<>();
    private Set<String> mExcludedPackages = new HashSet<>();

    // Packages which don't have launcher icons, but which we want to show nevertheless
    private static final String[] PACKAGE_WHITELIST = new String[] {
            "android",                          /* system server */
            "com.android.systemui",             /* system UI */
            "com.android.providers.downloads"   /* download provider */
    };

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            PackageItem item = (PackageItem) msg.obj;
            int index = Collections.binarySearch(mInstalledPackages, item);
            if (index < 0) {
                mInstalledPackages.add(-index - 1, item);
            } else {
                mInstalledPackages.get(index).activityTitles.addAll(item.activityTitles);
            }
            notifyDataSetChanged();
        }
    };

    public static class PackageItem implements Comparable<PackageItem> {
        public final String packageName;
        public final CharSequence title;
        private final TreeSet<CharSequence> activityTitles = new TreeSet<>();
        public final Drawable icon;

        PackageItem(String packageName, CharSequence title, Drawable icon) {
            this.packageName = packageName;
            this.title = title;
            this.icon = icon;
        }

        @Override
        public int compareTo(PackageItem another) {
            int result = title.toString().compareToIgnoreCase(another.title.toString());
            return result != 0 ? result : packageName.compareTo(another.packageName);
        }
    }

    public PackageListAdapter(Context context) {
        mPm = context.getPackageManager();
        mInflater = LayoutInflater.from(context);
        reloadList();
    }

    @Override
    public int getCount() {
        synchronized (mInstalledPackages) {
            return mInstalledPackages.size();
        }
    }

    @Override
    public PackageItem getItem(int position) {
        synchronized (mInstalledPackages) {
            return mInstalledPackages.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        synchronized (mInstalledPackages) {
            // packageName is guaranteed to be unique in mInstalledPackages
            return mInstalledPackages.get(position).packageName.hashCode();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.applist_preference_icon, null, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.title = convertView.findViewById(R.id.title);
            holder.summary = convertView.findViewById(R.id.summary);
            holder.icon = convertView.findViewById(R.id.icon);
            holder.divider = convertView.findViewById(R.id.divider);
        }

        PackageItem applicationInfo = getItem(position);
        holder.title.setText(applicationInfo.title);
        holder.icon.setImageDrawable(applicationInfo.icon);

        boolean needSummary = !applicationInfo.activityTitles.isEmpty();
        if (applicationInfo.activityTitles.size() == 1) {
            if (TextUtils.equals(applicationInfo.title, applicationInfo.activityTitles.first())) {
                needSummary = false;
            }
        }

        if (needSummary) {
            holder.summary.setText(TextUtils.join(", ", applicationInfo.activityTitles));
            holder.summary.setVisibility(View.VISIBLE);
        } else {
            holder.summary.setVisibility(View.GONE);
        }

        holder.divider.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);

        return convertView;
    }

    private void reloadList() {
        mInstalledPackages.clear();
        new Thread(this).start();
    }

    @Override
    public void run() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedAppsInfo = mPm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo info : installedAppsInfo) {
            ApplicationInfo appInfo = info.activityInfo.applicationInfo;
            if (mExcludedPackages.contains(appInfo.packageName)) {
                continue;
            }

            final PackageItem item = new PackageItem(appInfo.packageName,
                    appInfo.loadLabel(mPm), appInfo.loadIcon(mPm));
            item.activityTitles.add(info.loadLabel(mPm));
            mHandler.obtainMessage(0, item).sendToTarget();
        }

        for (String packageName : PACKAGE_WHITELIST) {
            if (mExcludedPackages.contains(packageName)) {
                continue;
            }
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(packageName, 0);
                final PackageItem item = new PackageItem(appInfo.packageName,
                        appInfo.loadLabel(mPm), appInfo.loadIcon(mPm));
                mHandler.obtainMessage(0, item).sendToTarget();
            } catch (PackageManager.NameNotFoundException ignored) {
                // package not present, so nothing to add -> ignore it
            }
        }
    }

    public void setExcludedPackages(HashSet<String> packages) {
        mExcludedPackages = packages;
        reloadList();
    }

    private static class ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;
        ImageView divider;
    }
}