package com.example.myapplication;


import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by xerox on 5/4/17.
 */

public class RequestTask extends AsyncTask<String, String, String> {

    MainActivity ctxt;
    public RequestTask(MainActivity act) {
        ctxt = act;
    }

    protected String doInBackground(String... uri){
        URL url;
        HttpURLConnection urlConnection=null;
        String response = "";
        try{
            //url=new URL("http://1-dot-cobalt-mind-162219.appspot.com/getOverview?lat=33.421839&lon=-111.944998&radius=100&time=10");
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            url=new URL(uri[0]+"&time=" + hour);

            urlConnection=(HttpURLConnection)url
                    .openConnection();

            InputStream in=urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
            InputStreamReader isw=new InputStreamReader(in);

            int data=isw.read();
            while(data!=-1){
                char current=(char)data;
                data=isw.read();
                System.out.print(current);
                response += current;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(urlConnection!=null){
                urlConnection.disconnect();
            }
        }
        return response;
    }
    protected void onPostExecute(String result) {
        ctxt.setOverview(result);
        ctxt.parseOverview();
        super.onPostExecute(result);
        //Do anything with response..
    }
}
