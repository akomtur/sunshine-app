package de.kaubisch.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kaubisch on 23.10.15.
 */
public class ForecastAdapter extends CursorAdapter {
    public static final int VIEW_TYPE_TODAY = 0;
    public static final int VIEW_TYPE_FUTURE_DAY = 1;
    private WeatherRepresentationCreator creator;
    private boolean useTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        creator = new WeatherRepresentationCreator(context);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    /*
            Remember that these views are reused as needed.
         */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int viewId;
        if(viewType == VIEW_TYPE_TODAY) {
            viewId = R.layout.list_item_forecast_today;
        } else {
            viewId = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate(viewId, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /*
                This is where we fill-in the views with the contents of the cursor.
             */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        boolean metric = Utility.isMetric(context);
        double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.high.setText(Utility.formatTemperature(maxTemp, context, metric));

        double minTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.low.setText(Utility.formatTemperature(minTemp, context, metric));

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.forecast.setText(description);

        String friendlyDayString = Utility.getFriendlyDayString(context, cursor.getLong(ForecastFragment.COL_WEATHER_DATE));
        viewHolder.date.setText(friendlyDayString);

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int iconResId = R.mipmap.ic_launcher;
        switch (getItemViewType(cursor.getPosition())) {
            case VIEW_TYPE_TODAY:
                iconResId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                iconResId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
        }
        viewHolder.icon.setImageResource(iconResId);
    }

    public static class ViewHolder {
        public final TextView high;
        public final TextView low;
        public final ImageView icon;
        public final TextView forecast;
        public final TextView date;

        public ViewHolder(final View view) {
            high = (TextView) view.findViewById(R.id.list_item_high_textview);
            low  = (TextView) view.findViewById(R.id.list_item_low_textview);
            icon = (ImageView) view.findViewById(R.id.list_item_icon);
            forecast = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            date = (TextView) view.findViewById(R.id.list_item_date_textview);
        }
    }
}