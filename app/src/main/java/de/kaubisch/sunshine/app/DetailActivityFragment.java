package de.kaubisch.sunshine.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.kaubisch.sunshine.app.data.WeatherContract;
import de.kaubisch.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER_ID = 1;

    public static String ARGUMENTS_URI = "uri";

    private static final String[] DETAIL_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_PRESSURE = 6;
    public static final int COL_WEATHER_WIND_SPEED = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;


    private ShareActionProvider shareProvider;

    private Uri detailUri;

    private String forecastString;

    public static DetailActivityFragment create(final Uri uri) {
        DetailActivityFragment fragment = new DetailActivityFragment();
        Bundle argBundle = new Bundle();
        argBundle.putParcelable(ARGUMENTS_URI, uri);
        fragment.setArguments(argBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        detailUri = getUri();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Loader<Cursor> loader = getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(detailUri != null) {
            return new CursorLoader(getActivity()
                    , detailUri
                    , DETAIL_COLUMNS
                    , null
                    , null
                    , null);
        }
        return null;
    }

    private Uri getUri() {
        return getArguments() != null ? (Uri)getArguments().get(ARGUMENTS_URI) : null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            Activity activity = getActivity();
            boolean isMetric = Utility.isMetric(activity);
            setTextToView(activity, R.id.detail_date_detail, Utility.getDayName(activity, data.getLong(COL_WEATHER_DATE)));
            setTextToView(activity, R.id.detail_date, Utility.getFormattedMonthDay(activity, data.getLong(COL_WEATHER_DATE)));
            setTextToView(activity, R.id.detail_temp_high, Utility.formatTemperature(data.getLong(COL_WEATHER_MAX_TEMP), activity, isMetric));
            setTextToView(activity, R.id.detail_temp_low, Utility.formatTemperature(data.getLong(COL_WEATHER_MIN_TEMP), activity, isMetric));
            setTextToView(activity, R.id.detail_forecast, data.getString(COL_WEATHER_DESC));
            setTextToView(activity, R.id.detail_wind, Utility.getFormattedWind(activity, data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGREES)));
            setTextToView(activity, R.id.detail_humidity, activity.getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY)));
            setTextToView(activity, R.id.detail_pressure, activity.getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE)));

            int condition = Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID));
            ImageView icon = (ImageView) activity.findViewById(R.id.detail_icon);
            icon.setImageResource(condition);
            setShareIntent(getDetailIntent());
        }
    }

    private void setTextToView(final Activity activity, final int viewId, String text) {
        ((TextView)activity.findViewById(viewId)).setText(text);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(forecastString != null) {
            setShareIntent(getDetailIntent());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private Intent getDetailIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, forecastString + " #sunshineApp");

        return intent;
    }


    private void setShareIntent(final Intent shareIntent) {
        if (shareProvider != null) {
            shareProvider.setShareIntent(shareIntent);
        }
    }

    public void onLocationChanged(String location) {
        Uri uri = detailUri;
        if(uri != null) {
            long dateFromUri = WeatherEntry.getDateFromUri(uri);
            Uri newUri = WeatherEntry.buildWeatherLocationWithDate(location, dateFromUri);
            detailUri = newUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
    }
}
