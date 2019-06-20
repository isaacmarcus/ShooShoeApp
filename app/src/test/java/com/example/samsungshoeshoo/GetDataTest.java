package com.example.samsungshoeshoo;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetDataTest{

    private String url="http://10.12.156.149:8100/shoes";

    @Test
    public void JsonObjectRequestShouldReturnJsonObjectResponse() {

        // Json Request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    // test for receiving a json array from response
                    assertNotNull(jsonArray);

                    for(int i=0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        TestCase.assertEquals("wrong JSon Format", "shelfIndex", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "occ", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "shoeIndex", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "type", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "colour", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "owner", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "img", jsonObject.keys().next());
                        TestCase.assertEquals("wrong JSon Format", "frequency", jsonObject.keys().next());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley",error.toString());
            }
        });

//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(jsonObjectRequest);
    }


}