package com.example.samsungshoeshoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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
import java.util.concurrent.ExecutionException;

public class MainHomePage extends AppCompatActivity {

    // Mock http from laptop
//    private String url="http://10.12.156.149:8100/shoes";
//    private String postUrl="http://10.12.156.149:8100/location";

    // urls from RPI
    private String url="http://10.12.0.18:8100/shoes"; // raspberry pi url
    private String postUrl="http://10.12.0.18:8100/location";

    // Setting up recycler view for favourites
    private RecyclerView favouritesRecyclerView;
    private MyHomeAdapter favouritesAdapter;
    private List<ListItem> favouritesItemList;

    // Setting up recycler view for extras
    private RecyclerView extraRecyclerView;
    private MyHomeAdapter extraAdapter;
    private List<ListItem> extraItemList;

    // Set up Recommended Img View Item List
    private RecyclerView recRecyclerView;
    private MyRecAdapter recAdapter;
    private List<ListItem> recItemList;

    // Sizing variables
    private static final float IMAGE_SIZE_RATIO = 0.27f;
    public static int screenWidth = 0;

    // Create network status boolean
    private static boolean connected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_main);

        // Pop up progress dialog to show loading database
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Database...");
        progressDialog.show();

        // Find buttons, using linear layout so buttons and text below will both trigger on click
        LinearLayout favButt = findViewById(R.id.favButt);
        LinearLayout sneakerButt = findViewById(R.id.sneakerButt);
        LinearLayout formalButt = findViewById(R.id.formalButt);
        LinearLayout slipperButt = findViewById(R.id.slipperButt);
        LinearLayout sandalButt = findViewById(R.id.sandalButt);

        // Build pull down to refresh view
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefreshHome);

        // Add different colours to refresh loading
        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // pull to refresh listener
        pullToRefresh.setOnRefreshListener(() -> {
            updatePage();
            pullToRefresh.setRefreshing(false);
        });

        // Method called to build recycler view on opening application
        buildRecyclerViews();

        // on click listeners for categories to deploy page
        favButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this, CategoryActivity.class);
            main_intent.putExtra("category","Favourites");
            startActivity(main_intent);
        });
        sneakerButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this, CategoryActivity.class);
            main_intent.putExtra("category","Sneakers");
            startActivity(main_intent);
        });
        formalButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this, CategoryActivity.class);
            main_intent.putExtra("category","Formals");
            startActivity(main_intent);
        });
        slipperButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this, CategoryActivity.class);
            main_intent.putExtra("category","Slippers");
            startActivity(main_intent);
        });
        sandalButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this, CategoryActivity.class);
            main_intent.putExtra("category","Sandals");
            startActivity(main_intent);
        });

        // Method for adjusting items in page based on screen size
        AdjustSizing();

        checkEmpty();
        progressDialog.dismiss();
    }

    public void deployItem(int position, MyHomeAdapter adapter, List<ListItem> itemList) {
        // Send Post Data

        JSONObject postData = new JSONObject();
        try {
            // PUT postData required to be received by server in JSON Format
            postData.put("shelfIndex", itemList.get(position).getShelfId());

            // execute AsyncTask to send post data to http server as JSONObject in string format
            SendDeviceDetails sDD = new SendDeviceDetails();
            JSONObject jsonResponse = new JSONObject(sDD.execute(postUrl, postData.toString()).get());
            String deployResponse = String.valueOf(jsonResponse.getBoolean("success"));

            if (deployResponse.equals("true")) {
                Toast.makeText(this, "Shoe being deployed...", Toast.LENGTH_SHORT).show();
                itemList.remove(position);
                adapter.notifyItemRemoved(position);
            } else if (deployResponse.equals("false")) {
                Toast.makeText(this, "Error, please try again later", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        updatePage();
    }

    public void deployRecItem(int position, MyRecAdapter adapter, List<ListItem> itemList) {
        // Send Post Data
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deploying Shoe...");
        progressDialog.show();

        JSONObject postData = new JSONObject();
        try {
            // PUT postData required to be received by server in JSON Format
            postData.put("shelfIndex", itemList.get(position).getShelfId());

            // execute AsyncTask to send post data to http server as JSONObject in string format
            SendDeviceDetails sDD = new SendDeviceDetails();
            JSONObject jsonResponse = new JSONObject(sDD.execute(postUrl, postData.toString()).get());
            String deployResponse = String.valueOf(jsonResponse.getBoolean("success"));
            progressDialog.dismiss();

            if (deployResponse.equals("true")) {
                Toast.makeText(this, "Shoe being deployed...", Toast.LENGTH_SHORT).show();
                itemList.remove(position);
                adapter.notifyItemRemoved(position);
            } else if (deployResponse.equals("false")) {
                Toast.makeText(this, "Error, please try again later", Toast.LENGTH_SHORT).show();
            }

            progressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        } catch (InterruptedException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        } catch (ExecutionException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }

        updatePage();
    }

    private void updatePage() {
        getDataRec(recAdapter, recItemList);
        getData("Favourites", favouritesAdapter, favouritesItemList);
        getData("Extras", extraAdapter, extraItemList);
//        getDataLocal("Favourites",favouritesItemList);
//        getDataLocal("Extras",extraItemList);
//        recAdapter.notifyDataSetChanged();
//        favouritesAdapter.notifyDataSetChanged();
//        extraAdapter.notifyDataSetChanged();
//        checkEmpty();

//        Thread updateThread = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    super.run();
//                    getDataRec(recAdapter, recItemList);
//                    getData("Favourites", favouritesAdapter, favouritesItemList);
//                    getData("Extras", extraAdapter, extraItemList);
//                } catch (Exception e) {
//                    Log.e("Thread Error", "Error");
//                } finally {
//                    recAdapter.notifyDataSetChanged();
//                    favouritesAdapter.notifyDataSetChanged();
//                    extraAdapter.notifyDataSetChanged();
//                    checkEmpty();
//                }
//            }
//        };
//        updateThread.start();
        Log.e("connected",Boolean.toString(connected));
    }

    // method to build recycler views
    public void buildRecyclerViews() {
        //**
        //
        // getting recycler view from xml
        //**
        recRecyclerView = findViewById(R.id.recRecyclerView);
        recRecyclerView.setHasFixedSize(true);
        favouritesRecyclerView = findViewById(R.id.favouritesRecyclerView);
        favouritesRecyclerView.setHasFixedSize(true);
        extraRecyclerView = findViewById(R.id.extraRecyclerView);
        extraRecyclerView.setHasFixedSize(true);

        // creating and setting horizontal linear layout managers for the recycler view
        recRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        extraRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        // initialize item list
        recItemList = new ArrayList<>();
        favouritesItemList = new ArrayList<>();
        extraItemList = new ArrayList<>();

        //creating recycler view adapter
        recAdapter = new MyRecAdapter(this, recItemList);
        favouritesAdapter = new MyHomeAdapter(this, favouritesItemList);
        extraAdapter = new MyHomeAdapter(this, extraItemList);

        // Get Database and update adapter
        updatePage();

        //setting adapter to recycler
        recRecyclerView.setAdapter(recAdapter);
        favouritesRecyclerView.setAdapter(favouritesAdapter);
        extraRecyclerView.setAdapter(extraAdapter);

        // on click listeners for deploy buttons
        favouritesAdapter.setOnItemClickListener(position -> deployItem(position, favouritesAdapter, favouritesItemList));
        extraAdapter.setOnItemClickListener(position -> deployItem(position, extraAdapter, extraItemList));
        recAdapter.setOnItemClickListener(position -> deployRecItem(position, recAdapter, recItemList));
    }

    private class GetDataAsync extends AsyncTask<JSONArray, Void, List<ListItem>> {

        @Override
        protected List<ListItem> doInBackground(JSONArray... params) {

            List<ListItem> currentItemList = new ArrayList<>();

            for(int i=0; i < params[0].length(); i++) {

                JSONObject jsonObject = null;
                // Add jsonObject only if the occupied field is 1
                try {
                    jsonObject = params[0].getJSONObject(i);
                    if(jsonObject.getString("occ").equals("1")) {
                        ListItem item = new ListItem();
                        item.setShelfId(Integer.parseInt(jsonObject.getString("shelfIndex")));
                        item.setOcc(Integer.parseInt(jsonObject.getString("occ")));
                        item.setType(jsonObject.getString("type"));
                        item.setColour(jsonObject.getString("colour"));
                        item.setOwner(jsonObject.getString("owner"));
                        item.setImage(jsonObject.getString("img"));
                        item.setDate(daysAgo(jsonObject.getString("timeStored")));

                        currentItemList.add(item);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return currentItemList;
        }

        @Override
        protected void onPostExecute(List<ListItem> result) {
            super.onPostExecute(result);
        }
    }

    // method to parse Data and attach to List
    private void getData(String currentCategory, MyHomeAdapter adapter, List<ListItem> itemList) {
        // Pop up progress dialog to show loading database
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Database...");
        progressDialog.show();

        // Json Request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, response -> {

            try {
                JSONArray jsonArray = response.getJSONArray("data");

                GetDataAsync gDA = new GetDataAsync();
                List<ListItem> currentItemList = gDA.execute(jsonArray).get();

                // switch case to add based on current category selected
                switch(currentCategory) {
                    case "Favourites":
                        Collections.sort(
                                currentItemList,
                                (item1, item2) -> item1.getDate() - item2.getDate());
                        break;
                    case "Extras":
                        Collections.sort(
                                currentItemList,
                                (item1, item2) -> item2.getDate() - item1.getDate());
                        break;
                    default:
                        break;
                }

                itemList.clear(); // empty itemList before putting items in it
                itemList.addAll(currentItemList);
                adapter.notifyDataSetChanged();
                // methods to check if empty or not connected
                connected = true;
                checkEmpty();
                progressDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            } catch (ExecutionException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }, error -> {
            Log.e("Volley",error.toString());
            connected = false; // change connected status to false if get volley connection error
            checkEmpty();
            progressDialog.dismiss();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    // method for getting and setting images for recommended shoes
    private void getDataRec(MyRecAdapter adapter, List<ListItem> itemList) {
//        // Find Image Views for Recommended shoes
//        ImageView recImgView1 = findViewById(R.id.recImgView1);
//        ImageView recImgView2 = findViewById(R.id.recImgView2);
//        ImageView recImgView3 = findViewById(R.id.recImgView3);
//
//        // add to list of recommended image views
//        ArrayList<ImageView> recImgList = new ArrayList<>();
//        recImgList.add(recImgView1);
//        recImgList.add(recImgView2);
//        recImgList.add(recImgView3);

        // Json Request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, response -> {

            try {
                List<ListItem> currentItemList = new ArrayList<>();

                JSONArray jsonArray = response.getJSONArray("data");
                for(int i=0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Add jsonObject only if the occupied field is 1 / black / slipper
                    if(jsonObject.getString("occ").equals("1") && (jsonObject.getString("colour").equals("black") || jsonObject.getString("type").equals("slippers"))) {
                        ListItem item = new ListItem();
                        item.setShelfId(Integer.parseInt(jsonObject.getString("shelfIndex")));
                        item.setOcc(Integer.parseInt(jsonObject.getString("occ")));
                        item.setType(jsonObject.getString("type"));
                        item.setColour(jsonObject.getString("colour"));
                        item.setOwner(jsonObject.getString("owner"));
                        item.setImage(jsonObject.getString("img"));
                        item.setDate(daysAgo(jsonObject.getString("timeStored")));

                        currentItemList.add(item);
                    }

                }

                itemList.clear(); // empty itemList before putting items in it
                itemList.addAll(currentItemList);
                adapter.notifyDataSetChanged();

//                for (int i = 0; i<3; i++) {
//                    try {
//                        byte[] decodedString = Base64.decode(itemList.get(i).getImage(), Base64.DEFAULT);
//                        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                        recImgList.get(i).setImageBitmap(decodedImg);
//                    } catch (IllegalArgumentException e) {
//                        e.printStackTrace();
//                        Log.e("Image received in wrong format", itemList.get(i).getImage());
//                    }
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("Volley",error.toString());
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    // Method to mock database, while testing offline
    private void getDataLocal(String currentCategory, List<ListItem> itemList) {
        itemList.clear();
        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "white",
                        "Johny",
                        "white_sneakers_m02",
                        daysAgo("Tue Jul 23 13:30:42 2019")));


        itemList.add(
                new ListItem(
                        2, 1,
                        "sneaker",
                        "black",
                        "Chrissy",
                        "black_sneakers_m04",
                        daysAgo("Thu Jul 25 13:30:42 2019")));

        itemList.add(
                new ListItem(
                        3, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02",
                        daysAgo("Wed Jul 24 13:30:42 2019")));

//        itemList.add(
//                new ListItem(
//                        1, 1,
//                        "sneaker",
//                        "red",
//                        "Teagen",
//                        "grey_sneakers_m02",
//                        "Thu Jul 25 13:30:42 2019"));
//
//        itemList.add(
//                new ListItem(
//                        1, 1,
//                        "sneaker",
//                        "blue",
//                        "Teagen",
//                        "grey_sneakers_m02",
//                        "Thu Jul 25 13:30:42 2019"));
//
//        itemList.add(
//                new ListItem(
//                        1, 1,
//                        "sneaker",
//                        "grey",
//                        "Teagen",
//                        "grey_sneakers_m02",
//                        "Thu Jul 25 13:30:42 2019"));
//
//        itemList.add(
//                new ListItem(
//                        1, 1,
//                        "sneaker",
//                        "grey",
//                        "Teagen",
//                        "grey_sneakers_m02",
//                        "Thu Jul 25 13:30:42 2019"));
        switch(currentCategory) {
            case "Favourites":
                Collections.sort(
                        itemList,
                        (item1, item2) -> item1.getDate() - item2.getDate());
                break;
            case "Extras":
                Collections.sort(
                        itemList,
                        (item1, item2) -> item2.getDate() - item1.getDate());
                break;
            default:
                break;
        }
    }

    private void AdjustSizing() {
        // Get the actual screen width
        screenWidth = getScreenWidth();
        // Calculate the new image size
        int imageViewSize = (int) ((float) screenWidth * IMAGE_SIZE_RATIO);
        // Calculate the margin
        int margin = (screenWidth - imageViewSize) / 2;

        // find temperature text view
        TextView tempTextView = findViewById(R.id.tempTextView);

        // Adjust Text View of temperature to include padding at top of status bar height
        ViewCompat.setOnApplyWindowInsetsListener(tempTextView, (v, insets) -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = insets.getSystemWindowInsetTop();
            return insets.consumeSystemWindowInsets();
        });
        ViewCompat.setOnApplyWindowInsetsListener(extraRecyclerView, (v, insets) -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = insets.getSystemWindowInsetBottom();
            return insets.consumeSystemWindowInsets();
        });
    }

    public int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        return width;
    }

    private void SetImgSize(ImageView imgView, int imgViewSize) {
        // Set the Image size
        RelativeLayout.LayoutParams recImgView1LayoutParams = (RelativeLayout.LayoutParams) imgView.getLayoutParams();
        // Setting the same size for Width and height
        recImgView1LayoutParams.height = imgViewSize;
        recImgView1LayoutParams.width = imgViewSize;
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

    private void checkEmpty() {
        // Find items to hide if list is empty
        TextView recTextView = findViewById(R.id.recTextView);
        TextView recTextView2 = findViewById(R.id.recTextView2);
        HorizontalScrollView buttonsScroll = findViewById(R.id.buttonsScroll);

        TextView favsTextView1 = findViewById(R.id.favouritesRec1TextView);
        TextView favsTextView2 = findViewById(R.id.favouritesRec2TextView);
        TextView extraTextView = findViewById(R.id.unusedRecTextView);

        ImageView greyOverlay = findViewById(R.id.grey_overlay);
        ImageView greyCardOverlay = findViewById(R.id.grey_overlay_weathercard);

        RecyclerView recRecyclerView = findViewById(R.id.recRecyclerView);
        RecyclerView favRecyclerView = findViewById(R.id.favouritesRecyclerView);
        RecyclerView extraRecyclerView = findViewById(R.id.extraRecyclerView);

        if (connected) {
            if (favouritesItemList.size() == 0) {
                Log.e("list: ","empty");
                recTextView.setText(getString(R.string.empty_title));
                recTextView2.setText(getString(R.string.empty_desc));
                recTextView2.setVisibility(View.VISIBLE);
                buttonsScroll.setVisibility(View.INVISIBLE);
                favsTextView1.setVisibility(View.INVISIBLE);
                favsTextView2.setVisibility(View.INVISIBLE);
                extraTextView.setVisibility(View.INVISIBLE);
                recRecyclerView.setVisibility(View.GONE);
                favRecyclerView.setVisibility(View.INVISIBLE);
                extraRecyclerView.setVisibility(View.INVISIBLE);
                greyOverlay.setVisibility(View.VISIBLE);
                greyCardOverlay.setVisibility(View.VISIBLE);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setNavigationBarColor(getColor(R.color.colorEmptyList));
                }
            } else {
                Log.e("list: ", "not empty");
                recTextView.setText("We recommend");
                recTextView2.setVisibility(View.GONE);
                buttonsScroll.setVisibility(View.VISIBLE);
                favsTextView1.setVisibility(View.VISIBLE);
                favsTextView2.setVisibility(View.VISIBLE);
                extraTextView.setVisibility(View.VISIBLE);
                recRecyclerView.setVisibility(View.VISIBLE);
                favRecyclerView.setVisibility(View.VISIBLE);
                extraRecyclerView.setVisibility(View.VISIBLE);
                greyOverlay.setVisibility(View.GONE);
                greyCardOverlay.setVisibility(View.GONE);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setNavigationBarColor(getColor(R.color.colorWhite));
                }
            }
        } else if (!connected) {
            recTextView.setText(getString(R.string.disc_title));
            recTextView2.setText(getString(R.string.disc_desc));
            recTextView2.setVisibility(View.VISIBLE);
            buttonsScroll.setVisibility(View.INVISIBLE);
            favsTextView1.setVisibility(View.INVISIBLE);
            favsTextView2.setVisibility(View.INVISIBLE);
            extraTextView.setVisibility(View.INVISIBLE);
            recRecyclerView.setVisibility(View.GONE);
            favRecyclerView.setVisibility(View.INVISIBLE);
            extraRecyclerView.setVisibility(View.INVISIBLE);
            greyOverlay.setVisibility(View.VISIBLE);
            greyCardOverlay.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(getColor(R.color.colorEmptyList));
            }
        }
    }

}
