package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    boolean isMapReady = false;
    boolean isDataReady = false;
    String nearbyData = "";
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
        float lat = sharedPref.getFloat("lat",0.0f);
        float lon = sharedPref.getFloat("lon",0.0f);
        new MapTask(this).execute("" +
                "?lat="+
                lat+"&lon="+lon+"&radius=.2" +
                "" +
                "]");

    }


    public void addMarkers() {
        try {
            JSONObject json = new JSONObject(nearbyData);
            JSONArray arr = json.getJSONArray("crimes");
            int length = arr.length();
            for (int i = 0; i < length; i++) {
                JSONObject crime = arr.getJSONObject(i);
                float lat = Float.parseFloat(crime.getString("lat"));
                float lon = Float.parseFloat(crime.getString("lon"));
                String type = crime.getString("type");
                addMarker(lat,lon, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        float lat = sharedPref.getFloat("lat", 0.0f);
        float lon = sharedPref.getFloat("lon", 0.0f);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat,lon))      // Sets the center of the map to Mountain View
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void updateData(String data) {
        nearbyData = data;
        isDataReady = true;
        if (isMapReady)
            addMarkers();
    }

    public void addMarker(float lat, float lon, String type) {
        LatLng mrk = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(mrk).title(type));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        isMapReady = true;
        if (isDataReady) {
            addMarkers();
        }
    }

}
