package com.example.samsungshoeshoo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendDeviceDetails extends AsyncTask<String, Void, String> {

    public String resResponse;

    @Override
    protected String doInBackground(String... params) {

        String data = "";

        HttpURLConnection httpURLConnection = null;
        try {

            // establish http connection to server
            httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            // create post request body
            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
            wr.writeBytes(params[1]); // write bytes from string of JSONObject
            wr.flush();
            wr.close();

            // Get data from http connection
            InputStream in = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);

            // Read data and store into data
            int inputStreamData = inputStreamReader.read();
            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                data += current;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        // return response message
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        resResponse = result;
        Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        try{
            JSONObject jsonResponse = new JSONObject(result);
            Log.d("Check", String.valueOf(jsonResponse.getBoolean("success")));
        } catch (JSONException err){
            Log.d("Error", err.toString());
        }
    }

}