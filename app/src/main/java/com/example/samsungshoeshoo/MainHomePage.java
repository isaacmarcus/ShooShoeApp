package com.example.samsungshoeshoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    // Sizing variables
    private static final float CARD_SIZE_RATIO = 0.7f;
    private static final float IMAGE_SIZE_RATIO = 0.27f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_main);

        // Find buttons, using linear layout so buttons and text below will both trigger on click
        LinearLayout favButt = findViewById(R.id.favButt);
        LinearLayout sneakerButt = findViewById(R.id.sneakerButt);
        LinearLayout formalButt = findViewById(R.id.formalButt);
        LinearLayout slipperButt = findViewById(R.id.slipperButt);

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

        // Pop up progress dialog to show loading database
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Database...");
        progressDialog.show();
        // Method called to build recycler view on opening application
        buildRecyclerViews();
        progressDialog.dismiss();

        // on click listeners for deploy buttons
        favouritesAdapter.setOnItemClickListener(position -> deployItem(position, favouritesAdapter, favouritesItemList));
        extraAdapter.setOnItemClickListener(position -> deployItem(position, extraAdapter, extraItemList));

        // on click listeners for categories to deploy page
        favButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this,MainActivity.class);
            main_intent.putExtra("category","Favourites");
            startActivity(main_intent);
        });
        sneakerButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this,MainActivity.class);
            main_intent.putExtra("category","Sneakers");
            startActivity(main_intent);
        });
        formalButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this,MainActivity.class);
            main_intent.putExtra("category","Formals");
            startActivity(main_intent);
        });
        slipperButt.setOnClickListener(v -> {
            Intent main_intent = new Intent(MainHomePage.this,MainActivity.class);
            main_intent.putExtra("category","Slippers");
            startActivity(main_intent);
        });

        // Method for adjusting items in page based on screen size
        AdjustSizing();
    }

    public void deployItem(int position, MyHomeAdapter adapter, List<ListItem> itemList) {
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
            sDD.execute(postUrl, postData.toString());

            progressDialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
        }

        //TODO: Add if statement for if message received from post is successful

        // Remove item if successfully deployed
        itemList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void updatePage() {
//        getData();
        getDataLocal(favouritesItemList);
        getDataLocal(extraItemList);
        favouritesAdapter.notifyDataSetChanged();
        extraAdapter.notifyDataSetChanged();
    }

    public void buildRecyclerViews() {
        //**
        //
        // getting recycler view from xml
        //**
        favouritesRecyclerView = findViewById(R.id.favouritesRecyclerView);
        favouritesRecyclerView.setHasFixedSize(true);
        extraRecyclerView = findViewById(R.id.extraRecyclerView);
        extraRecyclerView.setHasFixedSize(true);

        // creating and setting horizontal linear layout managers for the recycler view
        favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        extraRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        // initialize item list
        favouritesItemList = new ArrayList<>();
        extraItemList = new ArrayList<>();

        //creating recycler view adapter
        favouritesAdapter = new MyHomeAdapter(this, favouritesItemList);
        extraAdapter = new MyHomeAdapter(this, extraItemList);

        // Get Database and update adapter
        updatePage();

        //setting adapter to recycler
        favouritesRecyclerView.setAdapter(favouritesAdapter);
        extraRecyclerView.setAdapter(extraAdapter);
    }

    // method to parse Data and attach to List
    private void getData(String currentCategory, MyHomeAdapter adapter, List<ListItem> itemList) {

        // Json Request
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,null, response -> {

            try {
                List<ListItem> currentItemList = new ArrayList<>();

                JSONArray jsonArray = response.getJSONArray("data");
                for(int i=0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // Add jsonObject only if the occupied field is 1
                    if(jsonObject.getString("occ").equals("1")) {
                        ListItem item = new ListItem();
                        item.setShelfId(Integer.parseInt(jsonObject.getString("shelfIndex")));
                        item.setOcc(Integer.parseInt(jsonObject.getString("occ")));
                        item.setType(jsonObject.getString("type"));
                        item.setColour(jsonObject.getString("colour"));
                        item.setOwner(jsonObject.getString("owner"));
                        item.setImage(jsonObject.getString("img"));

                        // switch case to add based on current category selected
                        switch(currentCategory) {
                            case "Favourites":
                                currentItemList.add(item);
                                break;
                            case "Sneakers":
                                if(item.getType().equals("sneakers")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "Formal":
                                if(item.getType().equals("formal")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            case "Slippers":
                                if(item.getType().equals("slippers")) {
                                    currentItemList.add(item);
                                } else {
                                    break;
                                }
                            default:
                                break;
                        }

                    }

                }

                itemList.clear(); // empty itemList before putting items in it
                itemList.addAll(currentItemList);
                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
//                    progressDialog.dismiss();
            }
//                progressDialog.dismiss();
        }, error -> {
            Log.e("Volley",error.toString());
//                progressDialog.dismiss();
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);

    }

    // Method to mock database, while testing offline
    private void getDataLocal(List<ListItem> itemList) {
        itemList.clear();
        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "white",
                        "Johny",
                        "white_sneakers_m02"));


        itemList.add(
                new ListItem(
                        2, 1,
                        "sneaker",
                        "black",
                        "Chrissy",
                        "black_sneakers_m04"));

        itemList.add(
                new ListItem(
                        3, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "red",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "blue",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02"));

        itemList.add(
                new ListItem(
                        1, 1,
                        "sneaker",
                        "grey",
                        "Teagen",
                        "grey_sneakers_m02"));
    }

    private void AdjustSizing() {
        // Find Image Views for Recommended shoes
        ImageView recImgView1 = findViewById(R.id.recImgView1);
        ImageView recImgView2 = findViewById(R.id.recImgView2);
        ImageView recImgView3 = findViewById(R.id.recImgView3);

        // Get the actual screen width
        int screenWidth = getScreenWidth();
        // Calculate the new image size
        int imageViewSize = (int) ((float) screenWidth * IMAGE_SIZE_RATIO);
        // Calculate the margin
        int margin = (screenWidth - imageViewSize) / 2;

        // Call method to set image sizes for image views
        SetImgSize(recImgView1,imageViewSize);
        SetImgSize(recImgView2,imageViewSize);
        SetImgSize(recImgView3,imageViewSize);

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
//        int height = displayMetrics.heightPixels;
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


}
