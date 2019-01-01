/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.sunshine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String[]> {

    private static final String locationQueryKey = "queryLocation";
    private static final int LOADER_ID = 9;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter();

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData();
    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData() {
        showWeatherDataView();

        String location = SunshinePreferences.getPreferredWeatherLocation(this);

        Bundle bundle = new Bundle();
        bundle.putString(locationQueryKey, location);

        LoaderManager manager = getSupportLoaderManager();
        Loader loader = manager.getLoader(LOADER_ID);
        if (loader == null) {
            manager.initLoader(LOADER_ID, bundle, this);
        }
        else {
            manager.restartLoader(LOADER_ID, bundle, this);
        }
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int i, @Nullable final Bundle bundle) {
        return new AsyncTaskLoader<String[]>(this) {

            // TODO does this cached value persist through different calls? hope not
            private String[] cachedWeather;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                mLoadingIndicator.setVisibility(View.VISIBLE);
                if (cachedWeather != null) {
                    deliverResult(cachedWeather);
                }
                else {
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                if (bundle == null) {
                    return null;
                }
                String location = bundle.getString("queryLocation");
                URL weatherRequestUrl = NetworkUtils.buildUrl(location);

                try {
                    String jsonWeatherResponse = NetworkUtils
                            .getResponseFromHttpUrl(weatherRequestUrl);

                    String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                            .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                    return simpleJsonWeatherData;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable String[] data) {
                this.cachedWeather = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] weatherData) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (weatherData != null) {
            showWeatherDataView();
            mForecastAdapter.setWeatherData(weatherData);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        System.out.println("Getting called1");
        switch (id) {
            case R.id.action_refresh: return this.refreshData();
            case R.id.open_map: return this.openMap(item);
            case R.id.action_settings: return this.openSettings1();
            default: return super.onOptionsItemSelected(item);
        }

    }

    public boolean openSettings1() {
        System.out.println("Getting called");
        Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(startSettingsActivity);
        return true;
    }

    private boolean refreshData() {
        mForecastAdapter.setWeatherData(null);
        loadWeatherData();
        return true;
    }

    private boolean openMap(MenuItem item) {
        String address = "3814 Prospect Ave E, Cleveland, OH 44115";
        Uri uri = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", address)
                .appendQueryParameter("z", "20").build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            return true;
        }
        else {
            Log.d(this.toString(), "Failed opening map. No app found");
            return super.onOptionsItemSelected(item);
        }
    }
}