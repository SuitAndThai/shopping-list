package com.example.shopping_list;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rthai
 * Date: 6/26/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionTask extends AsyncTask<URL, Void, String> {
    protected String doInBackground(URL... urls) {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        String contentAsString = "";

        try {
            URL url = urls[0];
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("debug_tag", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            contentAsString = readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (ProtocolException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        return contentAsString;
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    protected void onProgressUpdate(Integer... progress) {
//        setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long result) {
//        showDialog("Downloaded " + result + " bytes");
    }
}