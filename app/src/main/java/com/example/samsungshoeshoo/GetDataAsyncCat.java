package com.example.samsungshoeshoo;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GetDataAsyncCat extends AsyncTask<JSONArray, Void, List<ListItem>> {

    private Context mContext;

    public GetDataAsyncCat (Context context){
        mContext = context;
    }

    @Override
    protected List<ListItem> doInBackground(JSONArray... params) {
        List<ListItem> currentItemList = new ArrayList<>();

        try {
            for (int i = 0; i < params[0].length(); i++) {

                JSONObject jsonObject = params[0].getJSONObject(i);

                // Add jsonObject only if the occupied field is 1
                if (jsonObject.getString("occ").equals("1")) {
                    ListItem item = new ListItem();
                    item.setShelfId(Integer.parseInt(jsonObject.getString("shelfIndex")));
                    item.setOcc(Integer.parseInt(jsonObject.getString("occ")));
                    item.setType(jsonObject.getString("type"));
                    item.setColour(jsonObject.getString("colour"));
                    item.setOwner(jsonObject.getString("owner"));
                    item.setImage(jsonObject.getString("img"));
                    item.setDate(MainHomePage.daysAgo(jsonObject.getString("timeStored")));

                    // switch case to add based on current category selected
                    switch (CategoryActivity.currentCategory) {
                        case "Favourites":
                            currentItemList.add(item);
                            Collections.sort(
                                    currentItemList,
                                    (item1, item2) -> item1.getDate() - item2.getDate());
                            break;
                        case "Sneakers":
                            if (item.getType().equals("sneakers")) {
                                currentItemList.add(item);
                            } else {
                                break;
                            }
                        case "Formals":
                            if (item.getType().equals("formal")) {
                                currentItemList.add(item);
                            } else {
                                break;
                            }
                        case "Slippers":
                            if (item.getType().equals("slippers")) {
                                currentItemList.add(item);
                            } else {
                                break;
                            }
                        case "Sandals":
                            if (item.getType().equals("sandals")) {
                                currentItemList.add(item);
                            } else {
                                break;
                            }
                        default:
                            break;
                    }

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return currentItemList;
    }

    @Override
    protected void onPostExecute(List<ListItem> result) {
        super.onPostExecute(result);
    }


    public static int daysAgo(String oldDate) {
        // get and convert old date from String to Date format
        // Thu Jul 25 13:30:42 2019
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.US);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy", Locale.US);
//        Log.e(oldDate,"date received");

        // parse the date stored from database based on the formatter
        LocalDate startDate = LocalDate.parse(oldDate, formatter);

        // get current date
        LocalDate endDate = LocalDate.now();

        // calculate days ago
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return (Integer) (int) days;
    }
}