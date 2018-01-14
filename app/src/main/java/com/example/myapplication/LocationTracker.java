package com.example.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;


public class LocationTracker extends Service {
    static String overview_response;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    String url ="http://1-dot-hello-9239b.appspot.com/sample1";
    SharedPreferences sharedPref;
    LocalBroadcastManager broadcaster;

    public static final String LOCATION_DATA = "com.example.myapplication.LOCATION_DATA";
    public static final String MSG_TYPE_CRIME = "CRIME";
    public static final String MSG_TYPE_LOCATION = "LOCATION";
    static TextToSpeech sts = null;
    public void speak(String text) {
        sts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    void InitTextToSpeechInstance() {
        if (sts == null) {
            sts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    sts.setLanguage(Locale.US);
                }
            });
        }
    }

    void destroyTextToSpeechInstance() {
        if (sts!=null)
            sts.shutdown();
    }

    public LocationTracker() {
        Log.e(TAG, "CONSTRUCTOR");

    }

    protected void putNotification(String txt) {

        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 3, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("SafetyMeter")
                        .setContentText(txt)
                        .setContentIntent(pendingIntent); //Required on Gingerbread and below
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(1000), mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;


        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            float lat = (float)location.getLatitude();
            float lon = (float)location.getLongitude();
            //sendResult("" + lat + "," + lon, MSG_TYPE_LOCATION);
            SharedPreferences.Editor editor= sharedPref.edit();
            editor.putFloat("lat", lat);
            editor.putFloat("lon", lon);
            editor.commit();
            new BackgroundTask(LocationTracker.this).execute("http://1-dot-cobalt-mind-162219.appspot.com/getOverview?lat="+lat+"&lon="+lon+"&radius=0.2");

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        broadcaster = LocalBroadcastManager.getInstance(this);
        InitTextToSpeechInstance();
        initializeLocationManager();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(LocationTracker.this);
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    public void update(String resp) {
        overview_response= resp;
    }

    public void parse() {
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


            int crime_tr = js.getInt("crime_in_timerange");
            int total_crime = js.getInt("total_crime");
            float pc = crime_tr*100.0f/total_crime;
            String time_range = js.getString("time_range");
            int start = Integer.parseInt(time_range.split(":")[0]);
            int end = Integer.parseInt(time_range.split(":")[1]);

            String outputString="total Crime:"+ total_crime+" Time Factor:"+crime_tr+ " " + arg_max;
            String viewString="total crime around you:"+ total_crime+"\n"+pc+ "% of total crime happens from " +start+ " to "+end +
                    "\nYou are most vulnerable to " + arg_max+".\n\n "+  max*100.0f/crime_tr + " of crime was " + arg_max;
            Log.e("TAG", "Notification " + outputString);
            putNotification(outputString);
            Log.e("TAG", "Result " + outputString);
            sendResult(viewString, MSG_TYPE_CRIME);
            speak(viewString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * Broadcast message.
     * Currently MainActivity is registered listener.
     */
    public void sendResult(String message, String messageType) {
        Intent intent = new Intent(LOCATION_DATA);
        if(message != null) {
            //System.out.println("Sending "+ message);
            intent.putExtra(messageType, message);
        }
        broadcaster.sendBroadcast(intent);
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }


        super.onDestroy();
        destroyTextToSpeechInstance();
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
