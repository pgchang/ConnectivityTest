package com.example.patrick.connectivitytest;

import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;
import java.util.Date;

import android.widget.TextView;
import android.widget.EditText;
import android.view.View;
import android.os.AsyncTask;
import android.util.Base64;

import android.provider.Settings.Secure;
import android.provider.Settings;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "HttpExample";
    private EditText downloadUrlText;
    private EditText uploadUrlText;
    private EditText paramText;
    private TextView resultText;



    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadUrlText = (EditText) findViewById(R.id.myDownloadUrl);
        paramText = (EditText) findViewById(R.id.param);
        uploadUrlText = (EditText) findViewById(R.id.myUploadUrl);
        resultText = (TextView) findViewById(R.id.results);
        if (isNetworkAvailable()) {
            Log.d("MyActivity", "Network connection is available");
            try {
                Log.d("PLEASE READ ME", downloadUrl("android.com"));
            } catch (IOException e) {
                Log.d("PLEASE READ ME", "you done goofed");
                e.printStackTrace();
            }
        } else {
            Log.d("MyActivity", "Network connection is NOT available");
        }

    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myGetHandler(View view) {
        // Gets the URL from the UI's text field.
        String stringUrl = downloadUrlText.getText().toString();
        Log.i("READ ME PLEASE", stringUrl);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            //textView.setText("No network connection available.");
            Log.i("READ ME PLEASE", "No connection available");
        }
    }

    public void myPostHandler(View view) {
        //gets the URL form the UI's text field.
        String stringUrl = uploadUrlText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new UploadWebpageTask().execute(stringUrl);
        } else {
            //textView.setText("No network connection available.");
            Log.i("READ ME PLEASE", "No connection available");
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.i("READ ME PLEASE", "activeNetworkInfo:" + activeNetworkInfo);
        Log.i("READ ME PLEASE", "activeNetworkInfo.isConnected():" + activeNetworkInfo.isConnected());
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                final String result = downloadUrl(urls[0]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultText.setText(result);
                    }
                });
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }



        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //textView.setText(result);
            Log.i("PLEASE READ ME", result);
        }

    }

    private class UploadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                final String result = uploadUrl(urls[0]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultText.setText(result);
                    }
                });
                return uploadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }



        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //textView.setText(result);
            Log.i("PLEASE READ ME", result);
        }
    }

    //Given a URL, establishes an HTTPUrlConnection and uploads
    // the data given to it via HTTPPost
    private String uploadUrl(String myurl) throws IOException {
        InputStream is = null;
        int len = 2500;
        URL url = new URL(myurl);
        String paramString = paramText.getText().toString();
        String urlParameter = "param1="+paramString;
        byte[] postData = urlParameter.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(postData, Base64.DEFAULT);
        int postDataLength = postData.length;
        byte[] uploadData = myurl.getBytes(StandardCharsets.UTF_8);
        int uploadDataLength = uploadData.length;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        //conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);

        try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
            wr.write( postData );
        }


        String offsetAmount = base64Encoder(timeZoneBuilder());
        //DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        //wr.write(uploadData);
        conn.getInputStream();
        String android_id = Settings.Secure.getString(getBaseContext().getContentResolver(),Secure.ANDROID_ID);
        //String contentAsString = readIt(is, len);
        return "uploading data";
    }
    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        String lastTimestamp = "21600";
        // Only display the first 2500 characters of the retrieved
        // web page content.
        int len = 2500;

        try {


            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000  /*milliseconds */); // don't forget the comments
            conn.setConnectTimeout(15000 /* milliseconds */);

            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("READ ME", "The response is: " + response);

            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            Log.d("READ ME", contentAsString);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String timeZoneBuilder() {
        String timeZone = "";
        Date now = new Date();
        int offsetFromUTC = TimeZone.getDefault().getOffset(now.getTime())/3600000;
        String offsetAmount = Integer.toString(offsetFromUTC);
        if (offsetFromUTC>=0) {
            timeZone = timeZone + "+";
        } else if (offsetFromUTC<0) {
            timeZone = timeZone + "-";
        }
        if (Math.abs(offsetFromUTC)<10) {
            timeZone = timeZone + "0";
        }
        timeZone = timeZone + Integer.toString(Math.abs(offsetFromUTC))+ ":00" ;
        return timeZone;
    }
    private String base64Encoder (String stringToEncode) {
        byte[] stringToByte = stringToEncode.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(stringToByte, Base64.DEFAULT);
        return base64;
    }


}

