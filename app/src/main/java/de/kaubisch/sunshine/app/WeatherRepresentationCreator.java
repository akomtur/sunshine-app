package de.kaubisch.sunshine.app;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by kaubisch on 23.10.15.
 */
public class WeatherRepresentationCreator {


    private Context mContext;

    public WeatherRepresentationCreator(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, mContext, isMetric) + "/" + Utility.formatTemperature(low, mContext, isMetric);
        return highLowStr;
    }


    public String createWeatherString(final Cursor cursor) {
        // get row indices for our cursor

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }
}
