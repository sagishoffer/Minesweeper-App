package com.example.sagi.mines;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HighScoreMapFragment extends Fragment implements OnMapReadyCallback {
    public static final String DATA_KEY = "data_key";

    private ArrayList<HashMap<String, String>> data;

    private MapFragment googleMap;
    private CameraPosition savedCameraPosition;

    public HighScoreMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            data = (ArrayList<HashMap<String, String>>) getArguments().getSerializable(HighScoreMapFragment.DATA_KEY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        GoogleMapOptions mapOptions = new GoogleMapOptions();
        mapOptions.zoomControlsEnabled(true).compassEnabled(true);

        if (savedCameraPosition != null) {
            mapOptions.camera(savedCameraPosition);
        } else {

        }

        // Check if GPS is turned on and display message
        checkForGPS();

        googleMap = MapFragment.newInstance(mapOptions);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, googleMap);
        fragmentTransaction.commit();

        googleMap.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        initHighScoreMap(data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        googleMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                savedCameraPosition = googleMap.getCameraPosition();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_high_score_map, container, false);
    }

    public void initHighScoreMap(final ArrayList<HashMap<String, String>> data) {
        googleMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                for (int i = 0; i < data.size(); i++) {
                    HashMap<String, String> row = data.get(i);
                    String name = row.get(DBHelper.HIGHSCORES_COLUMN_NAME);
                    String time = row.get(DBHelper.HIGHSCORES_COLUMN_TIME);
                    //String address = row.get(DBHelper.HIGHSCORES_COLUMN_ADDRESS);

                    double lng = Double.parseDouble(row.get(DBHelper.HIGHSCORES_COLUMN_LNG));
                    double lat = Double.parseDouble(row.get(DBHelper.HIGHSCORES_COLUMN_LAT));

                    if (lng != -1 && lat != -1) {
                        LatLng loction = new LatLng(lat, lng);
                        String title = name + " - " + time + " seconds";
                        googleMap.addMarker(new MarkerOptions()
                                .position(loction)
                                .title(title));
                    }
                }
            }
        });
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
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    public void checkForGPS() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnabled) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setMessage("GPS is disabled in your device. Enable it?")
                    .setCancelable(false).setPositiveButton("Enable GPS",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getActivity().startActivity(callGPSSettingIntent);
                        }
                    });

            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }
}
