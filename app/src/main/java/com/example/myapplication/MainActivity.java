package com.example.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import android.util.Log;
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedPref;
    BroadcastReceiver receiver;

    String overview_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, LocationTracker.class);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Log.e("bakchodi","ho rahi hai");
        startService(intent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        float lat = sharedPref.getFloat("lat",0.0f);
        float lon = sharedPref.getFloat("lon",0.0f);
        new RequestTask(this).execute("http://1-dot-cobalt-mind-162219.appspot.com/getOverview?lat="+lat+"&lon="+lon+"&radius=0.2");
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
                String s = intent.getStringExtra(LocationTracker.CRIME_DATA);
                tvResponse.setText(s);
                Log.e("TAG" , "Main " + s);
                // do something here.
            }
        };
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
                new IntentFilter(LocationTracker.CRIME_DATA)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}