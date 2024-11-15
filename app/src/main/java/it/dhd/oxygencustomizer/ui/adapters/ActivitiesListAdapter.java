package it.dhd.oxygencustomizer.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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

public class ActivitiesListAdapter extends BaseAdapter implements Runnable {

    private final Context mContext;
    private final PackageManager mPm;
    private final LayoutInflater mInflater;
    private final List<ActivityItem> mInstalledPackages = new LinkedList<>();
    private Set<String> mExcludedActivities = new HashSet<>();
    private String mPackageName;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            ActivityItem item = (ActivityItem) msg.obj;
            int index = Collections.binarySearch(mInstalledPackages, item);
            if (index < 0) {
                mInstalledPackages.add(-index - 1, item);
            }
            notifyDataSetChanged();
        }
    };

    public static class ActivityItem implements Comparable<ActivityItem> {
        public final String activityName;
        public final CharSequence title;
        public final Drawable icon;

        ActivityItem(String packageName, CharSequence title, Drawable icon) {
            this.activityName = packageName;
            this.title = title;
            this.icon = icon;
        }

        @Override
        public int compareTo(ActivityItem another) {
            int result = title.toString().compareToIgnoreCase(another.title.toString());
            return result != 0 ? result : activityName.compareTo(another.activityName);
        }
    }

    public ActivitiesListAdapter(Context context) {
        mContext = context;
        mPm = context.getPackageManager();
        mInflater = LayoutInflater.from(context);
        reloadList();
    }

    public void setPackageName(String pkgName) {
        mExcludedActivities.clear();
        mPackageName = pkgName;
        reloadList();
    }

    @Override
    public int getCount() {
        synchronized (mInstalledPackages) {
            return mInstalledPackages.size();
        }
    }

    @Override
    public ActivityItem getItem(int position) {
        synchronized (mInstalledPackages) {
            return mInstalledPackages.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        synchronized (mInstalledPackages) {
            // packageName is guaranteed to be unique in mInstalledPackages
            return mInstalledPackages.get(position).activityName.hashCode();
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

        ActivityItem applicationInfo = getItem(position);
        holder.title.setText(applicationInfo.title);
        holder.icon.setImageDrawable(applicationInfo.icon);
        holder.summary.setText(String.join("/n", mPackageName, applicationInfo.activityName));
        holder.summary.setVisibility(View.VISIBLE);
        holder.divider.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);

        return convertView;
    }

    private void reloadList() {
        mInstalledPackages.clear();
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mPackageName)) return;

        try {
            ApplicationInfo appInfo = mPm.getApplicationInfo(mPackageName, 0);
            Drawable appIcon = mPm.getApplicationIcon(appInfo);
            PackageInfo info = mPm.getPackageInfo(mPackageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] list = info.activities;
            for (ActivityInfo activityInfo : list) {
                if (mExcludedActivities.contains(activityInfo.targetActivity)) {
                    continue;
                }
                String label = activityInfo.loadLabel(mPm).toString();
                if (label.isEmpty()) {
                    label = activityInfo.name;
                }
                final ActivityItem item = new ActivityItem(activityInfo.name,
                        label,
                        appIcon);
                mHandler.obtainMessage(0, item).sendToTarget();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("ActivitiesListAdapter", "Package not found: " + mPackageName, e);
            return;
        }


    }

    public void setExcludedActivities(HashSet<String> packages) {
        mExcludedActivities = packages;
        reloadList();
    }

    private static class ViewHolder {
        TextView title;
        TextView summary;
        ImageView icon;
        ImageView divider;
    }
}
