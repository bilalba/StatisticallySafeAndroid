package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.github.mikephil.charting.utils.ColorTemplate.JOYFUL_COLORS;

public class TimewiseActivity extends AppCompatActivity {
    String data = "";
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timewise);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(TimewiseActivity.this);
        float lat = sharedPref.getFloat("lat",0.0f);
        float lon = sharedPref.getFloat("lon",0.0f);
        new TimeTask(this).execute("http://1-dot-cobalt-mind-162219.appspot.com/getTimeData?lat="+lat+"&lon="+lon+"&radius=2");
    }


    public void update(View view) {
        float lat = sharedPref.getFloat("lat",0.0f);
        float lon = sharedPref.getFloat("lon",0.0f);
        EditText ed = (EditText) findViewById(R.id.editText);
        String l = ed.getText().toString();
        float rad =0;
        try {
             rad = Float.parseFloat(l);
        } catch (Exception e) {
            Snackbar.make(view, "Please add valid value", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        new TimeTask(this).execute("http://1-dot-cobalt-mind-162219.appspot.com/getTimeData?lat="+lat+"&lon="+lon+"&radius=" + rad);
    }
    public void updateData(String updatedData) {
        data = updatedData;
    }

    public void parseDataPlot() {

        try {
            JSONObject json = new JSONObject(data);
            JSONArray types_js = json.getJSONArray("types");
            String[] types = new String[types_js.length()];
            for (int i = 0; i < types.length; i++) {
                types[i] = types_js.getString(i);
            }
            int[][] freq_arr = new int[types.length+1][24];
            for (int i = 0; i < types.length; i++) {
                JSONArray freqs = json.getJSONArray(types[i]);
                for (int j=0; j< 24; j++) {
                    freq_arr[i][j] = freqs.getInt(j);
                }
            }
            JSONArray freqs = json.getJSONArray("total");
            for (int j=0; j< 24; j++) {
                freq_arr[types.length][j] = freqs.getInt(j);
            }
            // DONE WITH PARSING THE DATA!!!!


            LineChart chart = (LineChart) findViewById(R.id.chart);
            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            for (int i = 0; i < types.length; i++) {
                List<Entry> entries = new ArrayList<Entry>();
                for (int j = 0; j < 24; j++) {
                    entries.add(new Entry(j, freq_arr[i][j]));
                }
                LineDataSet lds = new LineDataSet(entries, types[i]);
                lds.setColor(JOYFUL_COLORS[((new Random()).nextInt(500))%JOYFUL_COLORS.length]);
                dataSets.add(lds);
            }
            LineData lineData = new LineData(dataSets);
            chart.setData(lineData);
            TextView tt = (TextView) findViewById(R.id.radius);
            tt.setText("Radius:" + json.getString("radius"));
            chart.invalidate();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
