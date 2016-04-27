package com.example.sagi.mines;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HighScoreTableFragment extends Fragment {
    public static final String DATA_KEY = "data_key";

    private ArrayList<HashMap<String, String>> data;
    private TableRow.LayoutParams rankParams, addressParams, nameParams, timeParams;

    public HighScoreTableFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            data = (ArrayList<HashMap<String, String>>)getArguments().getSerializable(HighScoreTableFragment.DATA_KEY);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        TableLayout table = (TableLayout) getView().findViewById(R.id.highScoresTable);
        table.removeAllViews();
        initHighScoreTable(data);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_high_score_table, container, false);
    }

    public void initHighScoreTable(ArrayList<HashMap<String, String>> data) {
        TableLayout table = (TableLayout) getView().findViewById(R.id.highScoresTable);
        initTableParams();
        initHeaders(table);

        for (int i = 0; i < data.size(); i++) {
            HashMap<String, String> rowData = data.get(i);
            TableRow newRow = new TableRow(getActivity());
            newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView rank = new TextView(getActivity());
            rank.setLayoutParams(rankParams);
            rank.setText("" + (i + 1));
            newRow.addView(rank);

            TextView name = new TextView(getActivity());
            name.setLayoutParams(nameParams);
            name.setText(rowData.get(DBHelper.HIGHSCORES_COLUMN_NAME));
            newRow.addView(name);

            TextView address = new TextView(getActivity());
            address.setLayoutParams(addressParams);
            double lng = Double.parseDouble(rowData.get(DBHelper.HIGHSCORES_COLUMN_LNG));
            double lat = Double.parseDouble(rowData.get(DBHelper.HIGHSCORES_COLUMN_LAT));
            address.setText(getAddressFromLocation(lat, lng));
            newRow.addView(address);

            TextView time = new TextView(getActivity());
            time.setLayoutParams(timeParams);
            time.setText(rowData.get(DBHelper.HIGHSCORES_COLUMN_TIME));
            newRow.addView(time);

            table.addView(newRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float densityDpi = metrics.density;
        Log.i("GameAcivity", "densityDpi = " + densityDpi);

        return (int)densityDpi*dp;
    }


    private void initHeaders(TableLayout table) {
        TableRow newRow = new TableRow(getActivity());

        TextView rank = new TextView(getActivity());
        rank.setLayoutParams(rankParams);
        rank.setText("#");
        rank.setTypeface(null, Typeface.BOLD);
        newRow.addView(rank);

        TextView name = new TextView(getActivity());
        name.setLayoutParams(nameParams);
        name.setText("Name");
        name.setTypeface(null, Typeface.BOLD);
        newRow.addView(name);

        TextView address = new TextView(getActivity());
        address.setLayoutParams(addressParams);
        address.setText("Address");
        address.setTypeface(null, Typeface.BOLD);
        newRow.addView(address);

        TextView time = new TextView(getActivity());
        time.setLayoutParams(timeParams);
        time.setText("Time");
        time.setTypeface(null, Typeface.BOLD);
        newRow.addView(time);

        table.addView(newRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private void initTableParams(){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int widthMargin = (int)getResources().getDimension(R.dimen.activity_horizontal_margin);
        int screenWidth = size.x - widthMargin*2 - (4*2)*20 - dpToPx(20);

        rankParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        rankParams.setMargins(20, 20, 20, 20);

        nameParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        nameParams.setMargins(20, 20, 20, 20);
        nameParams.width = screenWidth*3/10;

        addressParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        addressParams.setMargins(20, 20, 20, 20);
        addressParams.width = screenWidth*5/10;

        timeParams = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(20, 20, 20, 20);
        timeParams.width = screenWidth*2/10;
    }

    private String getAddressFromLocation(double latitude ,double longitude){
        String address = "", city = "", country = "";
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        if (latitude != -1 && longitude != -1) {
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                country = addresses.get(0).getCountryName();
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
            }
        }

        if (address.equals("") && city.equals("") && country.equals(""))
            return "";
        return country + ", " + city + ", " + address;
    }
}
