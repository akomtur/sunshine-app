package de.kaubisch.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Objects;

import de.kaubisch.sunshine.app.data.WeatherContract;
import de.kaubisch.sunshine.app.gcm.RegistrationIntentService;
import de.kaubisch.sunshine.app.sync.SunshineSyncAdapter;

public class MainActivity extends ActionBarActivity {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private String location;

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String FORECASTFRAGMENT_TAG = "forecast";

    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = Utility.getPreferredLocation(this);
        setContentView(R.layout.activity_main);
        ForecastFragment forecastFragment = getForecastFragment();
        final Context context = this;
        if(findViewById(R.id.weather_detail_container) != null) {
            twoPane = true;
            if(savedInstanceState == null) {
                Object item = forecastFragment.getForecastAdapter().getItem(0);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
            forecastFragment.getForecastAdapter().setUseTodayLayout(false);
            forecastFragment.setClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                    if(cursor != null) {
                        String locationSetting = Utility.getPreferredLocation(context);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.weather_detail_container,
                                        DetailActivityFragment.create(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(ForecastFragment.COL_WEATHER_DATE))))
                                .commit();
                    }
                }
            });
        } else {
            twoPane = false;
            getSupportActionBar().setElevation(0.0f);
            forecastFragment.setClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                    if(cursor != null) {
                        String locationSetting = Utility.getPreferredLocation(context);
                        Intent intent = new Intent(context, DetailActivity.class)
                                .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                        locationSetting, cursor.getLong(ForecastFragment.COL_WEATHER_DATE)
                                ));
                        startActivity(intent);
                    }
                }
            });
        }

        SunshineSyncAdapter.initializeSyncAdapter(this);
        if(checkPlayServices()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = preferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if(!sentToken) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }

    private ForecastFragment getForecastFragment() {
        return (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(MainActivity.class.getName(), "ondestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(MainActivity.class.getName(), "onpause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(MainActivity.class.getName(), "onstop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(MainActivity.class.getName(), "onresume");
        String savedLocation = Utility.getPreferredLocation(this);
        if(location != null && !location.equals(savedLocation)) {
            ForecastFragment fragment = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if(fragment != null) {
                fragment.onLocationChanged();
            }
            DetailActivityFragment detailFragment = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if(detailFragment != null) {
                detailFragment.onLocationChanged(location);
            }
            location = savedLocation;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(availability.isUserResolvableError(resultCode)) {
                availability.getErrorDialog(this, resultCode, 9000).show();
            } else {
                finish();
            }
            return false;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(MainActivity.class.getName(), "onstart");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingsActivity.navigateToActivity(this);
            return true;
        } else if(id == R.id.action_maps) {
            String location = Utility.getPreferredLocation(this);
            showMap(location);

        }

        return super.onOptionsItemSelected(item);
    }

    private void showMap(final String location) {
        Intent mapView = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        mapView.setData(uri);
        if(mapView.resolveActivity(getPackageManager()) != null) {
            startActivity(mapView);
        }

    }

}
