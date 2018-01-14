package com.example.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener,NavigationView.OnNavigationItemSelectedListener {
    Intent intent;
    BroadcastReceiver receiver;
    String overview_response;
    private GoogleMap mMap;
    boolean isMapReady = false;
    boolean isDataReady = false;
    String nearbyData = "";
    SharedPreferences sharedPref;
    final String GET_NEAREST_URL = "http://1-dot-cobalt-mind-162219.appspot.com/getNearest";
    float lat, lon;

    Circle mCurrLocationCircle1;
    Circle mCurrLocationCircle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        intent  =  new Intent(this, LocationTracker.class);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            Log.e("STATISTICALLYSAFE","permission already granted");

            sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);



            startService(intent);
            lat = sharedPref.getFloat("lat",0.0f);
            lon = sharedPref.getFloat("lon",0.0f);
            new RequestTask(this).execute("http://1-dot-cobalt-mind-162219.appspot.com/getOverview?lat="+lat+"&lon="+lon+"&radius=0.2");
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            String urlParams = "?lat=" + lat + "&lon=" + lon + "&radius=.2";
            new MapTask(this).execute(GET_NEAREST_URL + urlParams);
        }  else {
            ActivityCompat.requestPermissions(this, new String[] {
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    0);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView responseTextView = (TextView)findViewById(R.id.response);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final TextView tvResponse = (TextView) findViewById(R.id.response);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String crime = intent.getStringExtra(LocationTracker.MSG_TYPE_CRIME);
                if (crime!=null) {
                    tvResponse.setText(crime);
                    //Log.e("TAG" , "Main " + s);
                }

                String location = intent.getStringExtra(LocationTracker.MSG_TYPE_LOCATION);
                if (location!=null) {
                    String latLon[] = location.split(",");
                    if (latLon.length >= 2) {
                        lat = Float.parseFloat(latLon[0]);
                        lon = Float.parseFloat(latLon[1]);
                        drawCurrentLocation();
                    }
                }
                // do something here.
            }
        };



    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        String TAG = "logMesg";
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.e(TAG,"permission granted");

                   sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);



                    startService(intent);
                    lat = sharedPref.getFloat("lat",0.0f);
                    lon = sharedPref.getFloat("lon",0.0f);
                    new RequestTask(this).execute("http://1-dot-cobalt-mind-162219.appspot.com/getOverview?lat="+lat+"&lon="+lon+"&radius=0.2");

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Log.e(TAG,"permission not granted");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    protected void putNotification(String txt) {

        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 3, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        chart = (PieChart) findViewById(R.id.chart);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("SafetyMeter")
                        .setContentText(txt)
                        .setContentIntent(pendingIntent); //Required on Gingerbread and below
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }

    protected void setOverview(String resp) {
        overview_response = resp;
    }

    protected void parseOverview() {
        JSONObject js = null;
        try {
            js = new JSONObject(overview_response);

            JSONObject crime_type_timerange = js.getJSONObject("crime_by_type_in_timerange");
            JSONObject crime_type = js.getJSONObject("crime_by_type");
            Iterator<String> keys = crime_type_timerange.keys();

 //           List<PieEntry> entries = new ArrayList<PieEntry>();
            int count = 0;
            HashMap<String, Integer> type_freq = new HashMap<String, Integer>();
            HashMap<String, Integer> type_freq_tr = new HashMap<String, Integer>();
            int max = 0;
            String arg_max = "";
            while (keys.hasNext()) {
                String key = keys.next();
                int crr = crime_type_timerange.getInt(key);
//                entries.add(new PieEntry(crr,key));
                type_freq_tr.put(key,crr);
                if (crr> max){
                    arg_max = key;
                    max = crr;
                }

                type_freq.put(key,crime_type.getInt(key));
                count +=1;
            }

//            PieDataSet dataSet = new PieDataSet(entries,"rrr");
//            ArrayList<Integer> colors = new ArrayList<Integer>();
//        for (int c : ColorTemplate.VORDIPLOM_COLORS)
//            colors.add(c);

//            for (int c : ColorTemplate.JOYFUL_COLORS)
//                colors.add(c);

//        for (int c : ColorTemplate.COLORFUL_COLORS)
//            colors.add(c);

//            for (int c : ColorTemplate.LIBERTY_COLORS)
//                colors.add(c);

//        for (int c : ColorTemplate.PASTEL_COLORS)
//            colors.add(c);

            // colors.add(ColorTemplate.getHoloBlue());

 //           dataSet.setColors(colors);
//            PieData lineData = new PieData(dataSet);

//            chart.setData(lineData);
//            chart.setCenterText("Total:" + js.getInt("crime_in_timerange"));
//        chart.invalidate();

            int crime_tr = js.getInt("crime_in_timerange");
            int total_crime = js.getInt("total_crime");
            float pc = crime_tr*100.0f/total_crime;
            TextView responseTextView = (TextView)findViewById(R.id.response);
            String time_range = js.getString("time_range");
            int start = Integer.parseInt(time_range.split(":")[0]);
            int end = Integer.parseInt(time_range.split(":")[1]);

            responseTextView.setText("total crime around you:"+ total_crime+"\n"+pc+ "% of total crime happens from " +start+ " to "+end +
                    "\nYou are most vulnerable to " + arg_max+".\n\n "+  max*100.0f/crime_tr + " of crime was " + arg_max);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void drawCurrentLocation() {

        if (mMap!=null) {
            if (mCurrLocationCircle1!=null) {
                mCurrLocationCircle1.remove();
            }
            if (mCurrLocationCircle2!=null) {
                mCurrLocationCircle2.remove();
            }
            double delta = 1;
            if (mMap.getCameraPosition().zoom  < 10)
                delta = 500;
            mCurrLocationCircle1 = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(lat, lon))
                    .radius(9 * delta)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.WHITE));

             mCurrLocationCircle2 = mMap.addCircle(new CircleOptions()
                .center(new LatLng(lat, lon))
                .radius(4 * delta)
                .strokeColor(Color.BLUE)
                .fillColor(Color.BLUE));
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

         if (id == R.id.nav_gallery) {
            //case R.id.navigation_item_1:
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);

        } else if (id == R.id.nav_slideshow) {
            Intent i = new Intent(MainActivity.this, TimewiseActivity.class);
            startActivity(i);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(LocationTracker.LOCATION_DATA)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }


    public void addMarkers() {
        try {
            JSONObject json = new JSONObject(nearbyData);
            JSONArray arr = json.getJSONArray("crimes");
            int length = arr.length();
            if (arr.length() != 0)
                mMap.clear();

            for (int i = 0; i < length; i++) {
                JSONObject crime = arr.getJSONObject(i);
                float lat = Float.parseFloat(crime.getString("lat"));
                float lon = Float.parseFloat(crime.getString("lon"));
                String type = crime.getString("type");
                addMarker(lat,lon, type);
            }
            drawCurrentLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat,lon))      // Sets the center of the map to Mountain View
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        */
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
        mMap.setOnCameraIdleListener(this);
        if (isDataReady) {
            addMarkers();
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat,lon))      // Sets the center of the map to Mountain View
                .zoom(17)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    /*
     * onCameraIdle is called when camera view changes and is not changing anymore.
     */
    @Override
    public void onCameraIdle() {
        //Get lat lon of center of map
        System.out.println("OnCamerIdle");
        if (mMap.getCameraPosition().zoom >= 16.8) {
            double lat = mMap.getCameraPosition().target.latitude;
            double lon = mMap.getCameraPosition().target.longitude;
            String urlParams = "?lat=" + lat + "&lon=" + lon + "&radius=0.15" ;
            new MapTask(this).execute(GET_NEAREST_URL + urlParams);
        } else {
            mMap.clear();
        }

    }
}
