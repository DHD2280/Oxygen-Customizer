package it.dhd.oxygencustomizer.ui.activity;

/*
 * Copyright (C) 2017-2020 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.BaseActivity;
import it.dhd.oxygencustomizer.ui.drawables.TintedDrawableSpan;
import it.dhd.oxygencustomizer.utils.NetworkUtils;

public class LocationBrowseActivity extends BaseActivity {
    private static final String TAG = "LocationBrowseActivity";

    public static final String DATA_LOCATION_NAME = "location_name";
    public static final String DATA_LOCATION_LAT = "location_lat";
    public static final String DATA_LOCATION_LON = "location_lon";

    private List<LocationBrowseItem> mLocationBrowseList = new ArrayList<>();
    private LocagtionListAdapter mAdapter;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Handler mHandler = new Handler();
    private String mQueryString;

    private Runnable mQueryRunnable = new Runnable() {
        @Override
        public void run() {
            mExecutorService.submit(() -> getLocations(mQueryString));
        }
    };

    private static final String URL_PLACES =
            "https://secure.geonames.org/searchJSON?name_startsWith=%s&lang=%s&username=omnijaws&maxRows=20";


    private static class LocationBrowseItem {
        private String mCityExt;
        private String mCountryId;
        private String mCity;
        private double mLat;
        private double mLon;

        public LocationBrowseItem(String cityExt, String countryId, String city, double lat, double lon) {
            mCityExt = cityExt;
            mCountryId = countryId;
            mCity = city;
            mLat = lat;
            mLon = lon;
        }

        public String getCity() {
            return mCity;
        }

        public String getCityExt() {
            return mCityExt;
        }

        protected String getId() {
            return mCity + "," + mCountryId;
        }

        public double getLat() {
            return mLat;
        }

        public double getLon() {
            return mLon;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof LocationBrowseItem && getId().equals(((LocationBrowseItem) obj).getId());
        }
    }

    public class LocagtionListAdapter extends RecyclerView.Adapter<LocagtionListAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ViewHolder(inflater.inflate(R.layout.location_browse_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LocationBrowseItem city = mLocationBrowseList.get(position);

            ((TextView) holder.itemView.findViewById(R.id.location_city)).setText(city.getCity());
            ((TextView) holder.itemView.findViewById(R.id.location_city_ext)).setText(city.getCityExt());

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.putExtra(DATA_LOCATION_NAME, city.getCity());
                intent.putExtra(DATA_LOCATION_LAT, city.getLat());
                intent.putExtra(DATA_LOCATION_LON, city.getLon());
                setResult(Activity.RESULT_OK, intent);
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return mLocationBrowseList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(@NonNull View view) {
                super(view);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.location_browse_activity);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.custom_location_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText queryPattern = findViewById(R.id.query_pattern_text);
        queryPattern.setHint(prefixTextWithIcon(this, R.drawable.ic_search, queryPattern.getHint()));
        queryPattern.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mHandler.removeCallbacks(mQueryRunnable);
                mQueryString = s.toString();
                if (TextUtils.isEmpty(mQueryString)) {
                    hideProgress();
                    mLocationBrowseList.clear();
                    mAdapter.notifyDataSetChanged();
                } else {
                    showProgress();
                    mHandler.postDelayed(mQueryRunnable, 750);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mAdapter = new LocagtionListAdapter();
        RecyclerView queryList = findViewById(R.id.query_result);
        queryList.setAdapter(mAdapter);
        queryList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void getLocations(String input) {
        mLocationBrowseList.clear();

        try {
            String lang = Locale.getDefault().getLanguage().replaceFirst("_", "-");
            String url = String.format(URL_PLACES, Uri.encode(input.trim()), lang);
            String response = NetworkUtils.downloadUrlMemoryAsString(url);
            if (response != null) {
                JSONArray jsonResults = new JSONObject(response).getJSONArray("geonames");
                int count = jsonResults.length();
                Log.d(TAG, "URL = " + url + " returning a response of count = " + count);

                for (int i = 0; i < count; i++) {
                    JSONObject result = jsonResults.getJSONObject(i);

                    int population = result.has("population") ? result.getInt("population") : 0;
                    if (population == 0) {
                        continue;
                    }

                    String city = result.getString("name");
                    String country = result.getString("countryName");
                    String countryId = result.getString("countryId");
                    String adminName = result.has("adminName1") ? result.getString("adminName1") : "";
                    String cityExt = (TextUtils.isEmpty(adminName) ? "" : adminName + ", ") + country;
                    double lat = result.getDouble("lat");
                    double lon = result.getDouble("lng");

                    LocationBrowseItem locationItem = new LocationBrowseItem(cityExt, countryId, city, lat, lon);
                    if (!mLocationBrowseList.contains(locationItem)) {
                        mLocationBrowseList.add(locationItem);
                        if (mLocationBrowseList.size() == 5) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Received malformed location data input=" + input, e);
        } finally {
            mHandler.post(() -> {
                hideProgress();
                mAdapter.notifyDataSetChanged();
            });
        }
    }

    private void showProgress() {
        findViewById(R.id.query_progressbar).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        findViewById(R.id.query_progressbar).setVisibility(View.GONE);
    }

    public static CharSequence prefixTextWithIcon(Context context, int iconRes, CharSequence msg) {
        // Update the hint to contain the icon.
        // Prefix the original hint with two spaces. The first space gets replaced by the icon
        // using span. The second space is used for a singe space character between the hint
        // and the icon.
        SpannableString spanned = new SpannableString("  " + msg);
        spanned.setSpan(new TintedDrawableSpan(context, iconRes),
                0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return spanned;
    }

}
