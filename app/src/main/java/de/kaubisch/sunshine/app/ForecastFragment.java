package de.kaubisch.sunshine.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import de.kaubisch.sunshine.app.data.WeatherContract;
import de.kaubisch.sunshine.app.service.SunshineService;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    public static final String LIST_POSITION = "list_position";

    private ForecastAdapter forecastAdapter;

    private final static int FORECAST_LOADER_ID = 0;
    private Loader<Cursor> cursorLoader;
    private LoaderManager.LoaderCallbacks<Cursor> callback;

    private ListView listView;
    private int position;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public void onLocationChanged() {
        fetchWeatherData();
        getLoaderManager().restartLoader(FORECAST_LOADER_ID, null, callback);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        listView = (ListView) view.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        if(savedInstanceState != null) {
            position = savedInstanceState.getInt(LIST_POSITION, 0);
        }

        return view;
    }

    public ForecastAdapter getForecastAdapter() {
        return forecastAdapter;
    }

    public void setClickListener(final AdapterView.OnItemClickListener clickListener) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ForecastFragment.this.position = position;
                clickListener.onItemClick(parent, view, position, id);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        callback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                String preferredLocation = Utility.getPreferredLocation(getActivity());

                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(preferredLocation, System.currentTimeMillis());

                return new CursorLoader(getActivity()
                        , uri
                        , FORECAST_COLUMNS
                        , null
                        , null
                        , sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                forecastAdapter.swapCursor(data);
                listView.smoothScrollToPosition(position);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                forecastAdapter.swapCursor(null);
            }
        };
        cursorLoader = getLoaderManager().initLoader(FORECAST_LOADER_ID, null, callback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                fetchWeatherData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LIST_POSITION, position);
        super.onSaveInstanceState(outState);
    }

    private void fetchWeatherData() {
        FragmentActivity activity = getActivity();
        Intent intent = new Intent(activity, SunshineService.class);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        intent.putExtra("location", preferences.getString(getString(R.string.pref_location_key), ""));
        activity.startService(intent);
    }
}
