package com.example.samsungshoeshoo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendDeviceDetails extends AsyncTask<String, Integer, String> {

    private Context mContext;
    private ProgressDialog progressDialog;

    public SendDeviceDetails (Context context, ProgressDialog pgD){
        mContext = context;
        progressDialog = new ProgressDialog(context);
    }

    // method to run and send device details to RPI
    @Override
    protected String doInBackground(String... params) {
//        final ProgressDialog progressDialog = new ProgressDialog(mContext);
//        progressDialog.setMessage("Deploying Shoe...");
//        progressDialog.show();
//        pgD.setMessage("Deploying Shoe...");
//        pgD.show();
        Time now = new Time();
        now.setToNow();
        publishProgress(now.second);

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

    protected void onPreExecute() {
        progressDialog.setMessage("Deploying Shoe");
        progressDialog.show();
    }

    protected void onProgressUpdate(Integer params) {
        progressDialog.setMessage(params.toString());
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        try{
            JSONObject jsonResponse = new JSONObject(result);
            Log.d("Check", String.valueOf(jsonResponse.getBoolean("success")));
        } catch (JSONException err){
            Log.d("Error", err.toString());
        }
    }

}